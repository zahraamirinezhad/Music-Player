package org.oxycblt.auxio.music

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import androidx.core.text.isDigitsOnly

fun ContentResolver.queryCursor(
    uri: Uri,
    projection: Array<out String>,
    selector: String? = null,
    args: Array<String>? = null
) = query(uri, projection, selector, args, null)

fun <R> ContentResolver.useQuery(
    uri: Uri,
    projection: Array<out String>,
    selector: String? = null,
    args: Array<String>? = null,
    block: (Cursor) -> R
): R? = queryCursor(uri, projection, selector, args)?.use(block)

val String.id3GenreName: String
    get() = parseId3v1Genre() ?: parseId3v2Genre() ?: this

private fun String.parseId3v1Genre(): String? =
    when {
        // ID3v1 genres are a plain integer value without formatting, so in that case
        // try to index the genre table with such.
        isDigitsOnly() -> GENRE_TABLE.getOrNull(toInt())

        // CR and RX are not technically ID3v1, but are formatted similarly to a plain number.
        this == "CR" -> "Cover"
        this == "RX" -> "Remix"

        // Current name is fine.
        else -> null
    }

private fun String.parseId3v2Genre(): String? {
    val groups = (GENRE_RE.matchEntire(this) ?: return null).groups
    val genres = mutableSetOf<String>()

    // ID3v2 genres are far more complex and require string grokking to properly implement.
    // You can read the spec for it here: https://id3.org/id3v2.3.0#TCON
    // This implementation in particular is based off Mutagen's genre parser.

    // Case 1: Genre IDs in the format (INT|RX|CR). If these exist, parse them as
    // ID3v1 tags.
    val genreIds = groups[1]
    if (genreIds != null && genreIds.value.isNotEmpty()) {
        val ids = genreIds.value.substring(1 until genreIds.value.lastIndex).split(")(")
        for (id in ids) {
            id.parseId3v1Genre()?.let(genres::add)
        }
    }

    // Case 2: Genre names as a normal string. The only case we have to look out for are
    // escaped strings formatted as ((genre).
    val genreName = groups[3]
    if (genreName != null && genreName.value.isNotEmpty()) {
        if (genreName.value.startsWith("((")) {
            genres.add(genreName.value.substring(1))
        } else {
            genres.add(genreName.value)
        }
    }

    return genres.joinToString(separator = ", ").ifEmpty { null }
}

/** Regex that implements matching for ID3v2's genre format. */
private val GENRE_RE = Regex("((?:\\(([0-9]+|RX|CR)\\))*)(.+)?")

/**
 * A complete table of all the constant genre values for ID3(v2), including non-standard extensions.
 * Note that we do not translate these, as that greatly increases technical complexity.
 */
private val GENRE_TABLE =
    arrayOf(
        // ID3 Standard
        "Blues",
        "Classic Rock",
        "Country",
        "Dance",
        "Disco",
        "Funk",
        "Grunge",
        "Hip-Hop",
        "Jazz",
        "Metal",
        "New Age",
        "Oldies",
        "Other",
        "Pop",
        "R&B",
        "Rap",
        "Reggae",
        "Rock",
        "Techno",
        "Industrial",
        "Alternative",
        "Ska",
        "Death Metal",
        "Pranks",
        "Soundtrack",
        "Euro-Techno",
        "Ambient",
        "Trip-Hop",
        "Vocal",
        "Jazz+Funk",
        "Fusion",
        "Trance",
        "Classical",
        "Instrumental",
        "Acid",
        "House",
        "Game",
        "Sound Clip",
        "Gospel",
        "Noise",
        "AlternRock",
        "Bass",
        "Soul",
        "Punk",
        "Space",
        "Meditative",
        "Instrumental Pop",
        "Instrumental Rock",
        "Ethnic",
        "Gothic",
        "Darkwave",
        "Techno-Industrial",
        "Electronic",
        "Pop-Folk",
        "Eurodance",
        "Dream",
        "Southern Rock",
        "Comedy",
        "Cult",
        "Gangsta",
        "Top 40",
        "Christian Rap",
        "Pop/Funk",
        "Jungle",
        "Native American",
        "Cabaret",
        "New Wave",
        "Psychadelic",
        "Rave",
        "Showtunes",
        "Trailer",
        "Lo-Fi",
        "Tribal",
        "Acid Punk",
        "Acid Jazz",
        "Polka",
        "Retro",
        "Musical",
        "Rock & Roll",
        "Hard Rock",

        // Winamp extensions, more or less a de-facto standard
        "Folk",
        "Folk-Rock",
        "National Folk",
        "Swing",
        "Fast Fusion",
        "Bebob",
        "Latin",
        "Revival",
        "Celtic",
        "Bluegrass",
        "Avantgarde",
        "Gothic Rock",
        "Progressive Rock",
        "Psychedelic Rock",
        "Symphonic Rock",
        "Slow Rock",
        "Big Band",
        "Chorus",
        "Easy Listening",
        "Acoustic",
        "Humour",
        "Speech",
        "Chanson",
        "Opera",
        "Chamber Music",
        "Sonata",
        "Symphony",
        "Booty Bass",
        "Primus",
        "Porn Groove",
        "Satire",
        "Slow Jam",
        "Club",
        "Tango",
        "Samba",
        "Folklore",
        "Ballad",
        "Power Ballad",
        "Rhythmic Soul",
        "Freestyle",
        "Duet",
        "Punk Rock",
        "Drum Solo",
        "A capella",
        "Euro-House",
        "Dance Hall",
        "Goa",
        "Drum & Bass",
        "Club-House",
        "Hardcore",
        "Terror",
        "Indie",
        "Britpop",
        "Negerpunk",
        "Polsk Punk",
        "Beat",
        "Christian Gangsta",
        "Heavy Metal",
        "Black Metal",
        "Crossover",
        "Contemporary Christian",
        "Christian Rock",
        "Merengue",
        "Salsa",
        "Thrash Metal",
        "Anime",
        "JPop",
        "Synthpop",

        // Winamp 5.6+ extensions, also used by EasyTAG.
        // I only include this because post-rock is a based genre and deserves a slot.
        "Abstract",
        "Art Rock",
        "Baroque",
        "Bhangra",
        "Big Beat",
        "Breakbeat",
        "Chillout",
        "Downtempo",
        "Dub",
        "EBM",
        "Eclectic",
        "Electro",
        "Electroclash",
        "Emo",
        "Experimental",
        "Garage",
        "Global",
        "IDM",
        "Illbient",
        "Industro-Goth",
        "Jam Band",
        "Krautrock",
        "Leftfield",
        "Lounge",
        "Math Rock",
        "New Romantic",
        "Nu-Breakz",
        "Post-Punk",
        "Post-Rock",
        "Psytrance",
        "Shoegaze",
        "Space Rock",
        "Trop Rock",
        "World Music",
        "Neoclassical",
        "Audiobook",
        "Audio Theatre",
        "Neue Deutsche Welle",
        "Podcast",
        "Indie Rock",
        "G-Funk",
        "Dubstep",
        "Garage Rock",
        "Psybient"
    )
