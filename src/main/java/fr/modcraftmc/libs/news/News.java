package fr.modcraftmc.libs.news;

public record News(int id, String type, int idByType, String title, String description, String picturePath, String datePublished, String urlAccess) { }
