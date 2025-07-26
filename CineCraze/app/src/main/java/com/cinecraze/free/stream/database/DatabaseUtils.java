package com.cinecraze.free.stream.database;

import com.cinecraze.free.stream.database.entities.EntryEntity;
import com.cinecraze.free.stream.models.Entry;
import com.cinecraze.free.stream.models.Server;
import com.cinecraze.free.stream.models.Season;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {
    
    private static final Gson gson = new Gson();
    
    /**
     * Convert Entry API model to EntryEntity database entity
     */
    public static EntryEntity entryToEntity(Entry entry, String mainCategory) {
        EntryEntity entity = new EntryEntity();
        
        entity.setTitle(entry.getTitle());
        entity.setSubCategory(entry.getSubCategory());
        entity.setCountry(entry.getCountry());
        entity.setDescription(entry.getDescription());
        entity.setPoster(entry.getPoster());
        entity.setThumbnail(entry.getThumbnail());
        entity.setRating(entry.getRatingString());
        entity.setDuration(entry.getDuration());
        entity.setYear(entry.getYearString());
        entity.setMainCategory(mainCategory);
        
        // Convert complex objects to JSON strings
        entity.setServersJson(gson.toJson(entry.getServers()));
        entity.setSeasonsJson(gson.toJson(entry.getSeasons()));
        entity.setRelatedJson(gson.toJson(entry.getRelated()));
        
        return entity;
    }
    
    /**
     * Convert EntryEntity database entity to Entry API model
     */
    public static Entry entityToEntry(EntryEntity entity) {
        Entry entry = new Entry();
        
        // Use reflection to set private fields (since Entry doesn't have setters)
        try {
            setPrivateField(entry, "title", entity.getTitle());
            setPrivateField(entry, "subCategory", entity.getSubCategory());
            setPrivateField(entry, "country", entity.getCountry());
            setPrivateField(entry, "description", entity.getDescription());
            setPrivateField(entry, "poster", entity.getPoster());
            setPrivateField(entry, "thumbnail", entity.getThumbnail());
            setPrivateField(entry, "rating", entity.getRating());
            setPrivateField(entry, "duration", entity.getDuration());
            setPrivateField(entry, "year", entity.getYear());
            
            // Convert JSON strings back to objects
            Type serverListType = new TypeToken<List<Server>>(){}.getType();
            List<Server> servers = gson.fromJson(entity.getServersJson(), serverListType);
            setPrivateField(entry, "servers", servers);
            
            Type seasonListType = new TypeToken<List<Season>>(){}.getType();
            List<Season> seasons = gson.fromJson(entity.getSeasonsJson(), seasonListType);
            setPrivateField(entry, "seasons", seasons);
            
            Type entryListType = new TypeToken<List<Entry>>(){}.getType();
            List<Entry> related = gson.fromJson(entity.getRelatedJson(), entryListType);
            setPrivateField(entry, "related", related);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return entry;
    }
    
    /**
     * Convert list of EntryEntity to list of Entry
     */
    public static List<Entry> entitiesToEntries(List<EntryEntity> entities) {
        List<Entry> entries = new ArrayList<>();
        for (EntryEntity entity : entities) {
            entries.add(entityToEntry(entity));
        }
        return entries;
    }
    
    /**
     * Helper method to set private fields using reflection
     */
    private static void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}