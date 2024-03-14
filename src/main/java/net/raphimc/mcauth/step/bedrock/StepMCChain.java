/*
 * This file is part of MinecraftAuth - https://github.com/RaphiMC/MinecraftAuth
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.mcauth.step.bedrock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import net.raphimc.mcauth.MinecraftAuth;
import net.raphimc.mcauth.step.AbstractStep;
import net.raphimc.mcauth.step.xbl.StepXblXstsToken;
import net.raphimc.mcauth.util.CryptUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StepMCChain extends AbstractStep<StepXblXstsToken.XblXsts<?>, StepMCChain.MCChain> {

    public static final String MINECRAFT_LOGIN_URL = "https://multiplayer.minecraft.net/authentication";

    private static final String MOJANG_PUBLIC_KEY_BASE64 = "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V";
    private static final ECPublicKey MOJANG_PUBLIC_KEY;
    private static final int CLOCK_SKEW = 60;

    static {
        try {
            MOJANG_PUBLIC_KEY = (ECPublicKey) CryptUtil.EC_KEYFACTORY.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(MOJANG_PUBLIC_KEY_BASE64)));
        } catch (Throwable e) {
            throw new RuntimeException("Could not create Mojang public key", e);
        }
    }

    public StepMCChain(AbstractStep<?, StepXblXstsToken.XblXsts<?>> prevStep) {
        super(prevStep);
    }

    @Override
    public MCChain applyStep(HttpClient httpClient, StepXblXstsToken.XblXsts<?> prevResult) throws Exception {
        MinecraftAuth.LOGGER.info("Authenticating with Minecraft Services...");

        final KeyPairGenerator secp384r1 = KeyPairGenerator.getInstance("EC");
        secp384r1.initialize(new ECGenParameterSpec("secp384r1"));
        final KeyPair ecdsa384KeyPair = secp384r1.generateKeyPair();
        final ECPublicKey publicKey = (ECPublicKey) ecdsa384KeyPair.getPublic();
        final ECPrivateKey privateKey = (ECPrivateKey) ecdsa384KeyPair.getPrivate();

        final JsonObject postData = new JsonObject();
        postData.addProperty("identityPublicKey", Base64.getEncoder().encodeToString(publicKey.getEncoded()));

        final HttpPost httpPost = new HttpPost(MINECRAFT_LOGIN_URL);
        httpPost.setEntity(new StringEntity(postData.toString(), ContentType.APPLICATION_JSON));
        httpPost.addHeader("Authorization", "XBL3.0 x=" + prevResult.userHash() + ";" + prevResult.token());
        final String response = httpClient.execute(httpPost, new BasicResponseHandler());
        final JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
        final JsonArray chain = obj.get("chain").getAsJsonArray();
        if (chain.size() != 2) throw new IllegalStateException("Invalid chain size");

        final Jws<Claims> mojangJwt = Jwts.parserBuilder().setAllowedClockSkewSeconds(CLOCK_SKEW).setSigningKey(MOJANG_PUBLIC_KEY).build().parseClaimsJws(chain.get(0).getAsString());
        final ECPublicKey mojangJwtPublicKey = (ECPublicKey) CryptUtil.EC_KEYFACTORY.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(mojangJwt.getBody().get("identityPublicKey", String.class))));
        final Jws<Claims> identityJwt = Jwts.parserBuilder().setAllowedClockSkewSeconds(CLOCK_SKEW).setSigningKey(mojangJwtPublicKey).build().parseClaimsJws(chain.get(1).getAsString());

        final Map<String, Object> extraData = identityJwt.getBody().get("extraData", Map.class);
        final String xuid = (String) extraData.get("XUID");
        final UUID id = UUID.fromString((String) extraData.get("identity"));
        final String displayName = (String) extraData.get("displayName");

        if (!extraData.containsKey("titleId")) {
            MinecraftAuth.LOGGER.warn("Minecraft chain does not contain titleId! You might get kicked from some servers");
        }

        final MCChain result = new MCChain(
                publicKey,
                privateKey,
                chain.get(0).getAsString(),
                chain.get(1).getAsString(),
                xuid,
                id,
                displayName,
                prevResult
        );
        MinecraftAuth.LOGGER.info("Got MC Chain, name: " + result.displayName + ", uuid: " + result.id + ", xuid: " + result.xuid);
        return result;
    }

    @Override
    public MCChain refresh(HttpClient httpClient, MCChain result) throws Exception {
        if (result == null || result.isExpired()) return super.refresh(httpClient, result);

        return result;
    }

    @Override
    public MCChain fromJson(JsonObject json) throws Exception {
        final StepXblXstsToken.XblXsts<?> prev = this.prevStep != null ? this.prevStep.fromJson(json.getAsJsonObject("prev")) : null;
        return new MCChain(
                (ECPublicKey) CryptUtil.EC_KEYFACTORY.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(json.get("publicKey").getAsString()))),
                (ECPrivateKey) CryptUtil.EC_KEYFACTORY.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(json.get("privateKey").getAsString()))),
                json.get("mojangJwt").getAsString(),
                json.get("identityJwt").getAsString(),
                json.get("xuid").getAsString(),
                UUID.fromString(json.get("id").getAsString()),
                json.get("displayName").getAsString(),
                prev
        );
    }

    public static final class MCChain implements StepResult<StepXblXstsToken.XblXsts<?>> {

        private final ECPublicKey publicKey;
        private final ECPrivateKey privateKey;
        private final String mojangJwt;
        private final String identityJwt;
        private final String xuid;
        private final UUID id;
        private final String displayName;
        private final StepXblXstsToken.XblXsts<?> prevResult;

        public MCChain(ECPublicKey publicKey, ECPrivateKey privateKey, String mojangJwt, String identityJwt, String xuid, UUID id, String displayName,
                       StepXblXstsToken.XblXsts<?> prevResult) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
            this.mojangJwt = mojangJwt;
            this.identityJwt = identityJwt;
            this.xuid = xuid;
            this.id = id;
            this.displayName = displayName;
            this.prevResult = prevResult;
        }

        @Override
        public JsonObject toJson() {
            final JsonObject json = new JsonObject();
            json.addProperty("publicKey", Base64.getEncoder().encodeToString(this.publicKey.getEncoded()));
            json.addProperty("privateKey", Base64.getEncoder().encodeToString(this.privateKey.getEncoded()));
            json.addProperty("mojangJwt", this.mojangJwt);
            json.addProperty("identityJwt", this.identityJwt);
            json.addProperty("xuid", this.xuid);
            json.addProperty("id", this.id.toString());
            json.addProperty("displayName", this.displayName);
            if (this.prevResult != null) json.add("prev", this.prevResult.toJson());
            return json;
        }

        @Override
        public boolean isExpired() throws Exception {
            try {
                final Jws<Claims> mojangJwt = Jwts.parserBuilder().setAllowedClockSkewSeconds(CLOCK_SKEW).setSigningKey(MOJANG_PUBLIC_KEY).build().parseClaimsJws(this.mojangJwt);
                final ECPublicKey identityPublicKey = (ECPublicKey) CryptUtil.EC_KEYFACTORY.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(mojangJwt.getBody().get("identityPublicKey", String.class))));
                Jwts.parserBuilder().setAllowedClockSkewSeconds(CLOCK_SKEW).setSigningKey(identityPublicKey).build().parseClaimsJws(this.identityJwt);
                return false;
            } catch (JwtException e) {
                return true;
            }
        }

        public ECPublicKey publicKey() {
            return publicKey;
        }

        public ECPrivateKey privateKey() {
            return privateKey;
        }

        public String mojangJwt() {
            return mojangJwt;
        }

        public String identityJwt() {
            return identityJwt;
        }

        public String xuid() {
            return xuid;
        }

        public UUID id() {
            return id;
        }

        public String displayName() {
            return displayName;
        }

        @Override
        public StepXblXstsToken.XblXsts<?> prevResult() {
            return prevResult;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            MCChain that = (MCChain) obj;
            return Objects.equals(this.publicKey, that.publicKey) &&
                    Objects.equals(this.privateKey, that.privateKey) &&
                    Objects.equals(this.mojangJwt, that.mojangJwt) &&
                    Objects.equals(this.identityJwt, that.identityJwt) &&
                    Objects.equals(this.xuid, that.xuid) &&
                    Objects.equals(this.id, that.id) &&
                    Objects.equals(this.displayName, that.displayName) &&
                    Objects.equals(this.prevResult, that.prevResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(publicKey, privateKey, mojangJwt, identityJwt, xuid, id, displayName, prevResult);
        }

        @Override
        public String toString() {
            return "MCChain[" +
                    "publicKey=" + publicKey + ", " +
                    "privateKey=" + privateKey + ", " +
                    "mojangJwt=" + mojangJwt + ", " +
                    "identityJwt=" + identityJwt + ", " +
                    "xuid=" + xuid + ", " +
                    "id=" + id + ", " +
                    "displayName=" + displayName + ", " +
                    "prevResult=" + prevResult + ']';
        }

    }

}
