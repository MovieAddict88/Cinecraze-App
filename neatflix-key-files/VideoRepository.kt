package com.ericg.neatflix.data.repository

import com.ericg.neatflix.data.remote.ApiService
import com.ericg.neatflix.data.remote.response.VideoResponse
import com.ericg.neatflix.util.FilmType
import javax.inject.Inject

class VideoRepository @Inject constructor(private val api: ApiService) {

    suspend fun getMovieVideos(movieId: Int): VideoResponse {
        return api.getMovieVideos(movieId)
    }

    suspend fun getTvVideos(tvId: Int): VideoResponse {
        return api.getTvVideos(tvId)
    }

    fun getVidsrcUrl(mediaId: Int, filmType: FilmType, season: Int? = null, episode: Int? = null): String {
        return when (filmType) {
            FilmType.MOVIE -> "https://vidsrc.net/embed/movie/$mediaId"
            FilmType.TV -> {
                if (season != null && episode != null) {
                    "https://vidsrc.net/embed/tv/$mediaId/$season/$episode"
                } else {
                    "https://vidsrc.net/embed/tv/$mediaId"
                }
            }
        }
    }

    fun getVidsrcProUrl(mediaId: Int, filmType: FilmType, season: Int? = null, episode: Int? = null): String {
        return when (filmType) {
            FilmType.MOVIE -> "https://vidsrc.pro/embed/movie/$mediaId"
            FilmType.TV -> {
                if (season != null && episode != null) {
                    "https://vidsrc.pro/embed/tv/$mediaId/$season/$episode"
                } else {
                    "https://vidsrc.pro/embed/tv/$mediaId"
                }
            }
        }
    }

    fun getVidsrcToUrl(mediaId: Int, filmType: FilmType, season: Int? = null, episode: Int? = null): String {
        return when (filmType) {
            FilmType.MOVIE -> "https://vidsrc.to/embed/movie/$mediaId"
            FilmType.TV -> {
                if (season != null && episode != null) {
                    "https://vidsrc.to/embed/tv/$mediaId/$season/$episode"
                } else {
                    "https://vidsrc.to/embed/tv/$mediaId"
                }
            }
        }
    }

    fun getMultipleStreamUrls(mediaId: Int, filmType: FilmType, season: Int? = null, episode: Int? = null): List<String> {
        return listOf(
            getVidsrcUrl(mediaId, filmType, season, episode),
            getVidsrcProUrl(mediaId, filmType, season, episode),
            getVidsrcToUrl(mediaId, filmType, season, episode)
        )
    }
}