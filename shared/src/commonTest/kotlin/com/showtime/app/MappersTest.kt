package com.showtime.app

import com.showtime.app.data.mapper.parseGenresCsv
import com.showtime.app.data.mapper.toCastEntity
import com.showtime.app.data.mapper.toDetailDomain
import com.showtime.app.data.mapper.toDomain
import com.showtime.app.data.mapper.toEntity
import com.showtime.app.data.mapper.toImageEntities
import com.showtime.app.data.remote.dto.CastMemberDto
import com.showtime.app.data.remote.dto.GenreDto
import com.showtime.app.data.remote.dto.ImageEntryDto
import com.showtime.app.data.remote.dto.MovieDetailDto
import com.showtime.app.data.remote.dto.MovieImagesDto
import com.showtime.app.data.remote.dto.MovieListItemDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MappersTest {

    @Test
    fun listItemDto_to_entity_to_domain_roundTrips() {
        val dto = MovieListItemDto(
            imdbId = "tt0111161",
            title = "The Shawshank Redemption",
            year = 1994,
            imdbRating = 9.3f,
            imdbVotes = 3171582,
            posterPath = "/poster.jpg",
            genres = listOf(GenreDto(18, "Drama"), GenreDto(80, "Crime"))
        )

        val domain = dto.toEntity().toDomain()

        assertEquals(dto.imdbId, domain.imdbId)
        assertEquals(dto.title, domain.title)
        assertEquals(1994, domain.year)
        assertEquals(9.3f, domain.imdbRating)
        assertEquals(3171582, domain.imdbVotes)
        assertEquals("/poster.jpg", domain.posterPath)
        assertEquals(listOf(18 to "Drama", 80 to "Crime"), domain.genres.map { it.id to it.name })
        // toDomain() defaults — not favorite/watchlisted unless told so
        assertFalse(domain.isFavorite)
        assertFalse(domain.isWatchlisted)
    }

    @Test
    fun genresCsv_handles_empty_and_malformed() {
        assertTrue("".parseGenresCsv().isEmpty())
        // a malformed (non-numeric id) segment is dropped, valid ones survive
        assertEquals(listOf(28 to "Action"), "x:Bad,28:Action".parseGenresCsv().map { it.id to it.name })
    }

    @Test
    fun detailDto_to_entity_to_detailDomain_carriesDetailFields() {
        val dto = MovieDetailDto(
            imdbId = "tt0468569",
            title = "The Dark Knight",
            overview = "Batman raises the stakes.",
            year = 2008,
            runtime = 152,
            posterPath = "/p.jpg",
            backdropPath = "/b.jpg",
            imdbRating = 9.1f,
            imdbVotes = 3150267,
            tmdbRating = 8.5f,
            genres = listOf(GenreDto(28, "Action"))
        )

        val detail = dto.toEntity().toDetailDomain(cast = emptyList(), isFavorite = true, isWatchlisted = false)

        assertEquals("Batman raises the stakes.", detail.overview)
        assertEquals(152, detail.runtime)
        assertEquals("/b.jpg", detail.backdropPath)
        assertEquals(8.5f, detail.tmdbRating)
        assertEquals(listOf(28 to "Action"), detail.genres.map { it.id to it.name })
        assertTrue(detail.isFavorite)
        assertFalse(detail.isWatchlisted)
    }

    @Test
    fun castDto_to_entity_to_domain_andImages_split() {
        val cast = CastMemberDto(imdbId = "nm0000288", name = "Christian Bale", profilePath = "/c.jpg")
            .toCastEntity(movieId = "tt0468569", ordering = 0)
        assertEquals("nm0000288", cast.personId)
        assertEquals(0, cast.ordering)
        assertEquals("Christian Bale", cast.toDomain().name)
        assertEquals("nm0000288", cast.toDomain().imdbId)

        val images = MovieImagesDto(
            posters = listOf(ImageEntryDto("/p1.jpg")),
            backdrops = listOf(ImageEntryDto("/b1.jpg"), ImageEntryDto("/b2.jpg"))
        ).toImageEntities("tt0468569")
        assertEquals(3, images.size)
        assertEquals(1, images.count { it.kind == "poster" })
        assertEquals(2, images.count { it.kind == "backdrop" })
    }
}
