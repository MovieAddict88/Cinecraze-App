package com.cinecraze.free.stream.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;

import com.cinecraze.free.stream.database.entities.EntryEntity;

import java.util.List;

@Dao
public interface EntryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EntryEntity> entries);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EntryEntity entry);
    
    @Query("SELECT * FROM entries")
    List<EntryEntity> getAllEntries();
    
    @Query("SELECT * FROM entries WHERE main_category = :category")
    List<EntryEntity> getEntriesByCategory(String category);
    
    @Query("SELECT * FROM entries WHERE title LIKE '%' || :title || '%'")
    List<EntryEntity> searchByTitle(String title);
    
    @Query("SELECT COUNT(*) FROM entries")
    int getEntriesCount();
    
    @Query("DELETE FROM entries")
    void deleteAll();
    
    @Query("DELETE FROM entries WHERE main_category = :category")
    void deleteByCategory(String category);
    
    // Pagination queries
    @Query("SELECT * FROM entries ORDER BY title ASC LIMIT :limit OFFSET :offset")
    List<EntryEntity> getEntriesPaged(int limit, int offset);
    
    @Query("SELECT * FROM entries WHERE main_category = :category ORDER BY title ASC LIMIT :limit OFFSET :offset")
    List<EntryEntity> getEntriesByCategoryPaged(String category, int limit, int offset);
    
    @Query("SELECT * FROM entries WHERE title LIKE '%' || :title || '%' ORDER BY title ASC LIMIT :limit OFFSET :offset")
    List<EntryEntity> searchByTitlePaged(String title, int limit, int offset);
    
    @Query("SELECT COUNT(*) FROM entries WHERE main_category = :category")
    int getEntriesCountByCategory(String category);
    
    @Query("SELECT COUNT(*) FROM entries WHERE title LIKE '%' || :title || '%'")
    int getSearchResultsCount(String title);
}