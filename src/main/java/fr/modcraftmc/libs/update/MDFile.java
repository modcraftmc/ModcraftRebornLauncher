package fr.modcraftmc.libs.update;

public class MDFile {

    private String path;
    private String name;
    private String size;
    private String md5;

    public MDFile(String path, String name, String size, String md5) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.md5 = md5;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getMd5() {
        return md5;
    }
}
