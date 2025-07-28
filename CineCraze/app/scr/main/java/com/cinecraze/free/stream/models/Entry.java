package com.cinecraze.free.stream.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Entry model class for movie/show entries
 * 
 * Note: Rating and Year fields can contain mixed data types:
 * - Rating: can be float (8.2), int (8), or String ("TV-Y7")
 * - Year: can be int (2021) or String
 * The getter methods handle type conversion safely.
 */
public class Entry {

    @SerializedName("Title")
    private String title;

    @SerializedName("SubCategory")
    private String subCategory;

    @SerializedName("MainCategory")
    private String mainCategory;

    @SerializedName("Country")
    private String country;

    @SerializedName("Description")
    private String description;

    @SerializedName("Poster")
    private String poster;

    @SerializedName("Thumbnail")
    private String thumbnail;

    @SerializedName("Rating")
    private Object rating; // Can be float, int, or String

    @SerializedName("Duration")
    private String duration;

    @SerializedName("Year")
    private Object year; // Can be int or String

    @SerializedName("Servers")
    private List<Server> servers;

    @SerializedName("Seasons")
    private List<Season> seasons;

    @SerializedName("Related")
    private List<Entry> related;

    public String getTitle() {
        return title;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public String getCountry() {
        return country;
    }

    public String getDescription() {
        return description;
    }

    public String getPoster() {
        return poster;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public float getRating() {
        if (rating instanceof Number) {
            return ((Number) rating).floatValue();
        } else if (rating instanceof String) {
            try {
                return Float.parseFloat((String) rating);
            } catch (NumberFormatException e) {
                return 0.0f; // Default to 0 if string cannot be parsed
            }
        }
        return 0.0f; // Default value
    }

    public String getRatingString() {
        if (rating instanceof String) {
            return (String) rating;
        } else if (rating instanceof Number) {
            return String.valueOf(rating);
        }
        return "0";
    }

    public String getDuration() {
        return duration;
    }

    public int getYear() {
        if (year instanceof Number) {
            return ((Number) year).intValue();
        } else if (year instanceof String) {
            try {
                return Integer.parseInt((String) year);
            } catch (NumberFormatException e) {
                return 0; // Default to 0 if string cannot be parsed
            }
        }
        return 0; // Default value
    }

    public String getYearString() {
        if (year instanceof String) {
            return (String) year;
        } else if (year instanceof Number) {
            return String.valueOf(year);
        }
        return "0";
    }

    public List<Server> getServers() {
        return servers;
    }

    public List<Season> getSeasons() {
        return seasons;
    }

    public List<Entry> getRelated() {
        return related;
    }
}
