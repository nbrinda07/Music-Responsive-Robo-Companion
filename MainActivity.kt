package com.example.robotspotifyapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.robotspotifyapp.ui.theme.RobotSpotifyAppTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object GlobalDebug {
    var lastError: String = "No error yet"
}

// --- DATA CLASSES ---
data class Track(val id: String, val name: String, val artistName: String, val albumName: String)
data class AudioFeatures(
    val valence: Float,
    val energy: Float,
    val speechiness: Float,
    val danceability: Float,
    val tempo: Float,
    val acousticness: Float
)
data class CurrentlyPlaying(val track: Track, val isPlaying: Boolean)
enum class RobotEmotion { HAPPY_ENERGETIC, HAPPY_CHILL, COOL_RAP, AGGRESSIVE_RAP, SAD, ROMANTIC_SLOW, ROMANTIC_HAPPY, NEUTRAL }

// --- RAPID API SERVICE ---
class RapidAPIService {
    // SECURITY NOTE: If you are getting Error 429, you need a new Key!
    private val apiKey = "24ee109459mshd5c86152e662d03p1a64ccjsn3170958d94ad"
    private val baseUrl = "https://track-analysis.p.rapidapi.com"

    suspend fun getAudioFeatures(songName: String, artistName: String): AudioFeatures? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val encodedSong = URLEncoder.encode(songName, "UTF-8")
                val encodedArtist = URLEncoder.encode(artistName, "UTF-8")

                // FIX: Correct URL path /pktx/analysis
                val url = "$baseUrl/pktx/analysis?song=$encodedSong&artist=$encodedArtist"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-rapidapi-key", apiKey)
                    .addHeader("x-rapidapi-host", "track-analysis.p.rapidapi.com")
                    .get()
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let { parseAudioFeatures(it) }
                } else {
                    GlobalDebug.lastError = "API Error: ${response.code} ${response.message}"
                    null
                }
            } catch (e: Exception) {
                GlobalDebug.lastError = "Network Error: ${e.message}"
                e.printStackTrace()
                null
            }
        }
    }

    private fun parseAudioFeatures(json: String): AudioFeatures? {
        return try {
            val jsonObject = JSONObject(json)
            AudioFeatures(
                valence = jsonObject.optDouble("happiness", 50.0).toFloat(),
                energy = jsonObject.optDouble("energy", 50.0).toFloat(),
                speechiness = jsonObject.optDouble("speechiness", 10.0).toFloat(),
                danceability = jsonObject.optDouble("danceability", 50.0).toFloat(),
                tempo = jsonObject.optDouble("tempo", 120.0).toFloat(),
                acousticness = jsonObject.optDouble("acousticness", 50.0).toFloat()
            )
        } catch (e: Exception) {
            GlobalDebug.lastError = "JSON Parse Error: ${e.message}"
            null
        }
    }
}

// --- SMART ANALYZER ---
class SmartMusicAnalyzer(private val context: ComponentActivity) {
    private val rapidAPI = RapidAPIService()
    private val expressionEngine = RobotExpressionEngine()
    private var lastAudioFeatures: AudioFeatures? = null

    // ... (Your preloadedEmotions map stays the same) ...
    private val preloadedEmotions = mapOf(
        "sicko mode travis scott" to RobotEmotion.AGGRESSIVE_RAP,
        "blinding lights the weeknd" to RobotEmotion.HAPPY_ENERGETIC,
        "someone like you adele" to RobotEmotion.SAD,
        "humble. kendrick lamar" to RobotEmotion.COOL_RAP,
        "perfect ed sheeran" to RobotEmotion.ROMANTIC_SLOW,
        "bad guy billie eilish" to RobotEmotion.COOL_RAP,
        "shape of you ed sheeran" to RobotEmotion.HAPPY_ENERGETIC,
        "gods plan drake" to RobotEmotion.COOL_RAP,
        "antihero taylor swift" to RobotEmotion.HAPPY_CHILL,
        "as it was harry styles" to RobotEmotion.HAPPY_CHILL,

        "capital rap seedhe maut" to RobotEmotion.COOL_RAP,
        "mp3 seedhe maut" to RobotEmotion.COOL_RAP,
        "ktmn seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "maar kaat seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "kilas seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "aadat seedhe maut" to RobotEmotion.SAD,
        "banda down seedhe maut" to RobotEmotion.COOL_RAP,
        "madira seedhe maut" to RobotEmotion.SAD,
        "el matador seedhe maut" to RobotEmotion.COOL_RAP,
        "pickup seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "bas jhut seedhe maut" to RobotEmotion.SAD,
        "rahat seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "wajah seedhe maut" to RobotEmotion.SAD,
        "akela seedhe maut" to RobotEmotion.SAD,
        "dl91 fm seedhe maut" to RobotEmotion.COOL_RAP,
        "bechara seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "sike seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "barsaat seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "guldasta seedhe maut" to RobotEmotion.ROMANTIC_HAPPY,
        "pancake seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "video games seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "mud seedhe maut" to RobotEmotion.COOL_RAP,
        "abaad seedhe maut" to RobotEmotion.HAPPY_ENERGETIC,
        "dil se seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "dhoke seedhe maut" to RobotEmotion.SAD,
        "bin tere seedhe maut" to RobotEmotion.SAD,
        "cd seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "cha chi seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "addict seedhe maut" to RobotEmotion.ROMANTIC_HAPPY,
        "kehne do seedhe maut" to RobotEmotion.ROMANTIC_HAPPY,

        "11k seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "sick proper seedhe maut" to RobotEmotion.COOL_RAP,
        "sick and proper seedhe maut" to RobotEmotion.COOL_RAP,
        "brand new seedhe maut" to RobotEmotion.COOL_RAP,
        "peace of mind seedhe maut" to RobotEmotion.COOL_RAP,
        "pushpak vimaan seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "dikkat seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "kya challa seedhe maut" to RobotEmotion.COOL_RAP,
        "fanne khan seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "first place seedhe maut" to RobotEmotion.COOL_RAP,
        "champions seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "baat aisi ghar jaisi seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "naam kaam sheher seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "pain seedhe maut" to RobotEmotion.COOL_RAP,
        "hausla seedhe maut" to RobotEmotion.SAD,
        "lunch break seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "asal g seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "swah seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "focused sedated seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "taakat seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "off beat seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "luka chippi seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "luka chuppi seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "khauf seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "i dont miss that life seedhe maut" to RobotEmotion.SAD,
        "akatsuki seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "khoon seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "w seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "joint in the booth seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "khatta flow seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "kavi seedhe maut" to RobotEmotion.SAD,
        "kehna chahte hain seedhe maut" to RobotEmotion.SAD,
        "namastute seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "nanchaku seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "101 seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "nawazuddin seedhe maut" to RobotEmotion.COOL_RAP,
        "hola amigo seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,

        "nayaab seedhe maut" to RobotEmotion.COOL_RAP,
        "toh kya seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "hoshiyaar seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "hosh seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "anaadi seedhe maut" to RobotEmotion.HAPPY_ENERGETIC,
        "dum ghutte seedhe maut" to RobotEmotion.SAD,
        "dum pishaach seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "maina seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "choti soch seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "godkod seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "gandi aulaad seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "khoj seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "kohra seedhe maut" to RobotEmotion.SAD,
        "jua seedhe maut" to RobotEmotion.COOL_RAP,
        "rajdhani seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "chidiya udd seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "batti seedhe maut" to RobotEmotion.COOL_RAP,
        "teen dost seedhe maut" to RobotEmotion.SAD,
        "marne ke baad bhi seedhe maut" to RobotEmotion.SAD,

        "kshama seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "ice seedhe maut" to RobotEmotion.COOL_RAP,
        "gourmet shit seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "moon comes up seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "red seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "round 3 seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "naraaz seedhe maut" to RobotEmotion.SAD,
        "brahmachari seedhe maut" to RobotEmotion.COOL_RAP,
        "shakti aur kshama seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "shakti seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "soyi nahi seedhe maut" to RobotEmotion.COOL_RAP,
        "raat ki rani seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "naksha seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "khush nahi seedhe maut" to RobotEmotion.ROMANTIC_SLOW,

        "intro seedhe maut" to RobotEmotion.COOL_RAP,
        "shaktimaan seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "gehraiyaan seedhe maut" to RobotEmotion.SAD,
        "uss din seedhe maut" to RobotEmotion.SAD,
        "meri baggi seedhe maut" to RobotEmotion.COOL_RAP,
        "dehshat seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "pnp seedhe maut" to RobotEmotion.COOL_RAP,
        "pankh seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "kyu seedhe maut" to RobotEmotion.SAD,
        "chalta reh seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "edokank seedhe maut" to RobotEmotion.COOL_RAP,

        "seedhe maut anthem seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "royalty seedhe maut" to RobotEmotion.COOL_RAP,
        "classsikh mautvolii seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "classsikh maut seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "stay calm seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "keh chukapt1 seedhe maut" to RobotEmotion.SAD,

        "kranti seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "pehchaan seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "jungli kutta seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "scalp dem seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "sim sim seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "magan seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "aankh band seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "mudda seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "shutdown seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "tt seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "bure din seedhe maut" to RobotEmotion.SAD,
        "tour shit seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "srk seedhe maut" to RobotEmotion.COOL_RAP,
        "namuna seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "kaanch ke ghar seedhe maut" to RobotEmotion.SAD,
        "kodak seedhe maut" to RobotEmotion.HAPPY_ENERGETIC,
        "bhussi seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "fire in the booth seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "mmm seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "tofa seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "tohfa seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "puff puff pass seedhe maut" to RobotEmotion.COOL_RAP,
        "ball seedhe maut" to RobotEmotion.COOL_RAP,
        "sanki seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "kpot seedhe maut" to RobotEmotion.COOL_RAP,
        "passion seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "nevermind seedhe maut" to RobotEmotion.SAD,
        "naamcheen seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "no enema seedhe maut" to RobotEmotion.COOL_RAP,
        "nafrat seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "na jaaye seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "nadaan seedhe maut" to RobotEmotion.SAD,
        "natkhat seedhe maut" to RobotEmotion.HAPPY_CHILL,
        "chalo chalein ritviz" to RobotEmotion.HAPPY_CHILL,
        "roshni sickflip" to RobotEmotion.HAPPY_CHILL,
        "bajenge badshah" to RobotEmotion.AGGRESSIVE_RAP,
        "bhundfaad rawal" to RobotEmotion.AGGRESSIVE_RAP,
        "do guna seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "yaad seedhe maut" to RobotEmotion.ROMANTIC_SLOW,
        "jama kar seedhe maut" to RobotEmotion.COOL_RAP,

        "yours truly krna" to RobotEmotion.COOL_RAP,
        "knock knock krna" to RobotEmotion.HAPPY_ENERGETIC,
        "sensitive krna" to RobotEmotion.COOL_RAP,
        "buss down krna" to RobotEmotion.AGGRESSIVE_RAP,
        "nothing to prove krna" to RobotEmotion.AGGRESSIVE_RAP,
        "talk my shit krna" to RobotEmotion.AGGRESSIVE_RAP,
        "kkbn krna" to RobotEmotion.AGGRESSIVE_RAP,
        "hello krna" to RobotEmotion.COOL_RAP,
        "vibrate krna" to RobotEmotion.HAPPY_CHILL,
        "who you are krna" to RobotEmotion.HAPPY_CHILL,
        "never enough krna" to RobotEmotion.AGGRESSIVE_RAP,

        "whats up krna" to RobotEmotion.HAPPY_CHILL,
        "stay away krna" to RobotEmotion.COOL_RAP,
        "shut up krna" to RobotEmotion.AGGRESSIVE_RAP,

        "far from over krna" to RobotEmotion.COOL_RAP,
        "no losses krna" to RobotEmotion.AGGRESSIVE_RAP,
        "prarthana krna" to RobotEmotion.AGGRESSIVE_RAP,
        "wanna know krna" to RobotEmotion.COOL_RAP,
        "hola amigo krna" to RobotEmotion.AGGRESSIVE_RAP,
        "still standing krna" to RobotEmotion.AGGRESSIVE_RAP,
        "some of us krna" to RobotEmotion.COOL_RAP,
        "role model krna" to RobotEmotion.AGGRESSIVE_RAP,

        "maharani krna" to RobotEmotion.AGGRESSIVE_RAP,
        "makasam krna" to RobotEmotion.AGGRESSIVE_RAP,
        "mera saamaan kahan hai krna" to RobotEmotion.AGGRESSIVE_RAP,
        "lil bunty krna" to RobotEmotion.AGGRESSIVE_RAP,
        "maem krna" to RobotEmotion.AGGRESSIVE_RAP,
        "seedha makeover krna" to RobotEmotion.AGGRESSIVE_RAP,
        "untitled krna" to RobotEmotion.AGGRESSIVE_RAP,
        "machayenge 4 krna" to RobotEmotion.AGGRESSIVE_RAP,
        "gawah rehna krna" to RobotEmotion.AGGRESSIVE_RAP,
        "blame the game krna" to RobotEmotion.COOL_RAP,

        "still here krna" to RobotEmotion.COOL_RAP,
        "roll up krna" to RobotEmotion.COOL_RAP,
        "villain krna" to RobotEmotion.AGGRESSIVE_RAP,
        "dream krna" to RobotEmotion.SAD,
        "fall off krna" to RobotEmotion.AGGRESSIVE_RAP,
        "saza e maut krna" to RobotEmotion.AGGRESSIVE_RAP,
        "what would the credit department do krna" to RobotEmotion.COOL_RAP,
        "living legend krna" to RobotEmotion.AGGRESSIVE_RAP,
        "na hai time krna" to RobotEmotion.COOL_RAP,
        "outta reach krna" to RobotEmotion.AGGRESSIVE_RAP,

        "kaha tak krna" to RobotEmotion.SAD,
        "no cap krna" to RobotEmotion.AGGRESSIVE_RAP,
        "been a while krna" to RobotEmotion.COOL_RAP,
        "og krna" to RobotEmotion.SAD,
        "ngl krna" to RobotEmotion.COOL_RAP,

        "10 pe 10 krna" to RobotEmotion.AGGRESSIVE_RAP,
        "zero after zero krna" to RobotEmotion.AGGRESSIVE_RAP,
        "no mercy krna" to RobotEmotion.AGGRESSIVE_RAP,
        "khauf krna" to RobotEmotion.AGGRESSIVE_RAP,
        "i guess krna" to RobotEmotion.COOL_RAP,
        "joota japani krna" to RobotEmotion.HAPPY_CHILL,
        "blowing up krna" to RobotEmotion.AGGRESSIVE_RAP,
        "say my name krna" to RobotEmotion.AGGRESSIVE_RAP,
        "farak nahi padta krna" to RobotEmotion.COOL_RAP,
        "playground krna" to RobotEmotion.HAPPY_ENERGETIC,
        "muqabla krna" to RobotEmotion.AGGRESSIVE_RAP,
        "dum hai krna" to RobotEmotion.AGGRESSIVE_RAP,
        "ashok chakra krna" to RobotEmotion.AGGRESSIVE_RAP,
        "showtime krna" to RobotEmotion.AGGRESSIVE_RAP,
        "voice of the streets krna" to RobotEmotion.AGGRESSIVE_RAP,
        "unstoppable krna" to RobotEmotion.AGGRESSIVE_RAP,
        "vijay krna" to RobotEmotion.AGGRESSIVE_RAP,
        "fos krna" to RobotEmotion.AGGRESSIVE_RAP,
        "touch base krna" to RobotEmotion.AGGRESSIVE_RAP,

        "vyanjan krna" to RobotEmotion.AGGRESSIVE_RAP,
        "kaisa mera desh krna" to RobotEmotion.SAD,
        "alone krna" to RobotEmotion.SAD,
        "freeverse feast krna" to RobotEmotion.AGGRESSIVE_RAP,
        "solo krna" to RobotEmotion.COOL_RAP,
        "alag krna" to RobotEmotion.HAPPY_CHILL,
        "dekho dekho krna" to RobotEmotion.AGGRESSIVE_RAP,
        "damn krna" to RobotEmotion.COOL_RAP,
        "down krna" to RobotEmotion.SAD,
        "last night krna" to RobotEmotion.ROMANTIC_SLOW,
        "im ready krna" to RobotEmotion.COOL_RAP,
        "getting away krna" to RobotEmotion.COOL_RAP,
        "tripping krna" to RobotEmotion.HAPPY_CHILL,
        "sellout krna" to RobotEmotion.COOL_RAP,

        "fall krna" to RobotEmotion.COOL_RAP,
        "keep it real krna" to RobotEmotion.COOL_RAP,
        "say my name english krna" to RobotEmotion.AGGRESSIVE_RAP,
        "who you are english krna" to RobotEmotion.HAPPY_CHILL,
        "roll up english krna" to RobotEmotion.COOL_RAP,

        "hola at your boy badshah" to RobotEmotion.HAPPY_ENERGETIC,
        "ykwim karan aujla" to RobotEmotion.COOL_RAP,
        "zero after zero munawar" to RobotEmotion.AGGRESSIVE_RAP,
        "khatta flow seedhe maut" to RobotEmotion.AGGRESSIVE_RAP,
        "zaruri nahi karma" to RobotEmotion.AGGRESSIVE_RAP,
        "tony montana karma" to RobotEmotion.AGGRESSIVE_RAP,
        "warm up karma" to RobotEmotion.AGGRESSIVE_RAP,
        "gallan karma" to RobotEmotion.ROMANTIC_SLOW,
        "on on karma" to RobotEmotion.HAPPY_CHILL,
        "khatarnaak karma" to RobotEmotion.AGGRESSIVE_RAP,
        "woh raat raftaar" to RobotEmotion.ROMANTIC_SLOW,
        "saath ya khilaaf raftaar" to RobotEmotion.AGGRESSIVE_RAP,
        "slay raftaar" to RobotEmotion.AGGRESSIVE_RAP,
        "quarantine young stunners" to RobotEmotion.AGGRESSIVE_RAP,
        "bag divine" to RobotEmotion.COOL_RAP,
        "baap se fotty seven" to RobotEmotion.AGGRESSIVE_RAP,
        "sankat fotty seven" to RobotEmotion.AGGRESSIVE_RAP,
        "high deep kalsi" to RobotEmotion.HAPPY_CHILL,
        "kala deep kalsi" to RobotEmotion.COOL_RAP,
        "sher deep kalsi" to RobotEmotion.AGGRESSIVE_RAP,
        "forever brodha v" to RobotEmotion.COOL_RAP,
        "jungle rawal" to RobotEmotion.AGGRESSIVE_RAP,
        "overdrive hi rez" to RobotEmotion.COOL_RAP,
        "crossroads hi rez" to RobotEmotion.SAD,
        "woofer dr zeus" to RobotEmotion.HAPPY_ENERGETIC,
        "kalamkaar cypher rashmeet kaur" to RobotEmotion.AGGRESSIVE_RAP,
        "halka halka rashmeet kaur" to RobotEmotion.ROMANTIC_SLOW,
        "batman aghor" to RobotEmotion.AGGRESSIVE_RAP,
        "click pow naught one" to RobotEmotion.AGGRESSIVE_RAP,


        "king shit shubh" to RobotEmotion.AGGRESSIVE_RAP,
        "safety off shubh" to RobotEmotion.AGGRESSIVE_RAP,
        "you and me shubh" to RobotEmotion.ROMANTIC_HAPPY,
        "hood anthem shubh" to RobotEmotion.HAPPY_CHILL,

        "still rollin intro shubh" to RobotEmotion.COOL_RAP,
        "still rollin shubh" to RobotEmotion.COOL_RAP,
        "cheques shubh" to RobotEmotion.COOL_RAP,
        "ice shubh" to RobotEmotion.AGGRESSIVE_RAP,
        "og shubh" to RobotEmotion.AGGRESSIVE_RAP,
        "ruthless shubh" to RobotEmotion.AGGRESSIVE_RAP,
        "dior shubh" to RobotEmotion.COOL_RAP,
        "the flow shubh" to RobotEmotion.COOL_RAP,

        "one love shubh" to RobotEmotion.ROMANTIC_HAPPY,
        "baller shubh" to RobotEmotion.HAPPY_ENERGETIC,
        "no love shubh" to RobotEmotion.SAD,
        "her shubh" to RobotEmotion.ROMANTIC_SLOW,
        "elevated shubh" to RobotEmotion.COOL_RAP,
        "offshore shubh" to RobotEmotion.COOL_RAP,
        "we rollin shubh" to RobotEmotion.AGGRESSIVE_RAP,

        "dont look irman thiara" to RobotEmotion.AGGRESSIVE_RAP,

        "raatan lambiyan ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,        // (Jan 2026 - Rock Fusion with Shinda)
        "thodi si daaru ap dhillon" to RobotEmotion.ROMANTIC_HAPPY,  // (ft. Shreya Ghoshal - Party Hit!)
        "afsos ap dhillon" to RobotEmotion.SAD,                       // (ft. Anuv Jain - Deep Sadness)
        "hitmen ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,           // (Nov 2025 - Heavy Trap)
        "without me ap dhillon" to RobotEmotion.ROMANTIC_SLOW,             // (Smooth Flex)
        "by my side ap dhillon" to RobotEmotion.ROMANTIC_SLOW,       // (Upbeat Love)
        "okay ap dhillon" to RobotEmotion.HAPPY_ENERGETIC,
        "stfu ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,      // (Aggressive)
        "never let you go steel banglez" to RobotEmotion.HAPPY_CHILL, // (ft. AP Dhillon & Omah Lay)

        "brownprint ap dhillon" to RobotEmotion.ROMANTIC_HAPPY,       // (Intro - Hard)
        "losing myself ap dhillon" to RobotEmotion.COOL_RAP,          // (ft. Gunna - Smooth Vibe)
        "bora bora ap dhillon" to RobotEmotion.HAPPY_ENERGETIC,           // (ft. Ayra Starr - Island Vibe)
        "315 ap dhillon" to RobotEmotion.HAPPY_ENERGETIC,              // (ft. Jazzy B - Folk Banger)
        "distance ap dhillon" to RobotEmotion.SAD,                    // (Emotional)
        "sweet flower ap dhillon" to RobotEmotion.ROMANTIC_SLOW,      // (Slow Burn)
        "old money ap dhillon" to RobotEmotion.COOL_RAP,              // (Flex)
        "after midnight ap dhillon" to RobotEmotion.SAD,              // (Late Night Feel)
        "to be continued ap dhillon" to RobotEmotion.COOL_RAP,

        "summer high ap dhillon" to RobotEmotion.ROMANTIC_SLOW,     // (Massive Dance Hit)
        "brown munde ap dhillon" to RobotEmotion.HAPPY_ENERGETIC,      // (The Anthem)
        "insane ap dhillon" to RobotEmotion.COOL_RAP,                 // (Iconic Vibe)
        "excuses ap dhillon" to RobotEmotion.HAPPY_ENERGETIC,                     // (Heartbreak Vibe)
        "with you ap dhillon" to RobotEmotion.ROMANTIC_HAPPY,          // (Acoustic Love)
        "true stories ap dhillon" to RobotEmotion.COOL_RAP,
        "ma belle ap dhillon" to RobotEmotion.ROMANTIC_SLOW,         // (Smiling Love)
        "desires ap dhillon" to RobotEmotion.ROMANTIC_SLOW,             // (Sad lyrics, upbeat vibe)
        "spaceship ap dhillon" to RobotEmotion.HAPPY_ENERGETIC,
        "tere te ap dhillon" to RobotEmotion.ROMANTIC_HAPPY,         // (Dance)
        "wo noor ap dhillon" to RobotEmotion.ROMANTIC_SLOW,           // (Poetic)
        "dil nu ap dhillon" to RobotEmotion.ROMANTIC_HAPPY,
        "hills ap dhillon" to RobotEmotion.SAD,
        "sleepless ap dhillon" to RobotEmotion.HAPPY_CHILL,

        "problems over peace ap dhillon" to RobotEmotion.COOL_RAP,    // (ft. Stormzy)
        "real talk ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,        // (ft. Shinda Kahlon)
        "scars ap dhillon" to RobotEmotion.ROMANTIC_SLOW,                       // (Documentary Track)
        "lifestyle ap dhillon" to RobotEmotion.COOL_RAP,              // (Documentary Track)

        "all night ap dhillon" to RobotEmotion.SAD,           // (Late Night Vibe)
        "final thoughts ap dhillon" to RobotEmotion.COOL_RAP,

        "fate ap dhillon" to RobotEmotion.SAD,
        "takeover ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,         // (ft. AR Paisley)
        "drip ap dhillon" to RobotEmotion.COOL_RAP,
        "foreigns ap dhillon" to RobotEmotion.COOL_RAP,
        "saada pyaar ap dhillon" to RobotEmotion.SAD,                 // (Deep Heartbreak)
        "goat ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,             // (Hard)
        "chances ap dhillon" to RobotEmotion.HAPPY_CHILL,

        "fake ap dhillon" to RobotEmotion.COOL_RAP,
        "faraar gurinder gill" to RobotEmotion.COOL_RAP,
        "top boy ap dhillon" to RobotEmotion.COOL_RAP,
        "arrogant ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,
        "deadly ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,
        "tochan ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,           // (Classic Banger)
        "droptop ap dhillon" to RobotEmotion.COOL_RAP,
        "free smoke ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,
        "most wanted ap dhillon" to RobotEmotion.COOL_RAP,
        "feels ap dhillon" to RobotEmotion.HAPPY_CHILL,
        "hustlin gminxr" to RobotEmotion.HAPPY_ENERGETIC,
        "kirsaan ap dhillon" to RobotEmotion.COOL_RAP,          // (Protest Anthem)
        "majhe aale ap dhillon" to RobotEmotion.HAPPY_CHILL,
        "majhail ap dhillon" to RobotEmotion.HAPPY_ENERGETIC,
        "toxic ap dhillon" to RobotEmotion.HAPPY_CHILL,
        "war ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,
        "against all odds ap dhillon" to RobotEmotion.COOL_RAP,
        "twisted personality ap dhillon" to RobotEmotion.AGGRESSIVE_RAP,
        "gallan ap dhillon" to RobotEmotion.ROMANTIC_SLOW,            // (Rare Melodic)

        "aadat yo yo honey singh" to RobotEmotion.HAPPY_CHILL,        // (ft. AP Dhillon)
        "crazy in love srmn" to RobotEmotion.ROMANTIC_SLOW,           // (ft. AP Dhillon)

        "i really do karan aujla" to RobotEmotion.ROMANTIC_HAPPY,      // (Melodic Intro)
        "for a reason karan aujla" to RobotEmotion.ROMANTIC_HAPPY,          // (Lifestyle Flex)
        "youre u tho karan aujla" to RobotEmotion.ROMANTIC_HAPPY,          // (Smooth)
        "boyfriend karan aujla" to RobotEmotion.ROMANTIC_HAPPY,       // (Upbeat Pop Love)
        "him karan aujla" to RobotEmotion.ROMANTIC_SLOW,                        // (Storytelling)
        "flipside karan aujla" to RobotEmotion.COOL_RAP,
        "ima do my thiiing karan aujla" to RobotEmotion.HAPPY_ENERGETIC, // (Summer Vibe)
        "daytona karan aujla" to RobotEmotion.COOL_RAP,               // (Cruising Vibe)
        "77 magnitude karan aujla" to RobotEmotion.AGGRESSIVE_RAP,   // (Hard Rap)
        "mf gabhru karan aujla" to RobotEmotion.AGGRESSIVE_RAP,       // (Hard Lead Single)
        "ppop culture karan aujla" to RobotEmotion.COOL_RAP,         // (Title Track)

        "tauba tauba karan aujla" to RobotEmotion.HAPPY_ENERGETIC,    // (Bad Newz - DANCING!)
        "winning speech karan aujla" to RobotEmotion.COOL_RAP,        // (Motivational)
        "wavy karan aujla" to RobotEmotion.HAPPY_CHILL,               // (Relaxed Summer Anthem)
        "courtside karan aujla" to RobotEmotion.COOL_RAP,
        "at peace karan aujla" to RobotEmotion.SAD,                   // (Introspective)
        "sifar safar karan aujla" to RobotEmotion.SAD,
        "aaye haaye afro mix karan aujla" to RobotEmotion.HAPPY_ENERGETIC, // (Dance/Club)
        "god damn karan aujla" to RobotEmotion.HAPPY_ENERGETIC,       // (Club Banger)
        "house of lies ikka" to RobotEmotion.COOL_RAP,
        "let it go karan aujla" to RobotEmotion.COOL_RAP,

        "idk how karan aujla" to RobotEmotion.COOL_RAP,               // (Smooth)
        "antidote karan aujla" to RobotEmotion.SAD,                   // (Melodic)
        "who they karan aujla" to RobotEmotion.AGGRESSIVE_RAP,        // (Hard)
        "ydg karan aujla" to RobotEmotion.COOL_RAP,                   // (Flex)
        "goin off karan aujla" to RobotEmotion.COOL_RAP,

        "100 million karan aujla" to RobotEmotion.COOL_RAP,           // (Flex)
        "nothing lasts karan aujla" to RobotEmotion.SAD,              // (Deep)
        "straight ballin karan aujla" to RobotEmotion.COOL_RAP,       // (Vibe)
        "top class karan aujla" to RobotEmotion.AGGRESSIVE_RAP,       // (Hard)
        "hisaab karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "yaad karan aujla" to RobotEmotion.SAD,                       // (Heartbreak)
        "tareefan karan aujla" to RobotEmotion.ROMANTIC_HAPPY,        // (Pop Vibe)

        "softly karan aujla" to RobotEmotion.ROMANTIC_HAPPY,                // (Peak Chill Vibe)
        "admirin you karan aujla" to RobotEmotion.ROMANTIC_HAPPY,     // (Upbeat Love)
        "jee ni lagda karan aujla" to RobotEmotion.ROMANTIC_HAPPY,               // (Sad Anthem)
        "try me karan aujla" to RobotEmotion.COOL_RAP,
        "what karan aujla" to RobotEmotion.ROMANTIC_SLOW,
        "bachke bachke karan aujla" to RobotEmotion.ROMANTIC_SLOW,   // (Flirty/Fun)
        "champions anthem karan aujla" to RobotEmotion.COOL_RAP,          // (Relaxed Celebration)
        "girl i love you karan aujla" to RobotEmotion.ROMANTIC_SLOW,  // (Slow Love)
        "52 bars karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "you karan aujla" to RobotEmotion.ROMANTIC_SLOW,

        "oouuu karan aujla" to RobotEmotion.COOL_RAP,
        "they know karan aujla" to RobotEmotion.COOL_RAP,
        "game over karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "unreachable karan aujla" to RobotEmotion.COOL_RAP,
        "gangsta karan aujla" to RobotEmotion.AGGRESSIVE_RAP,

        "ykwim karan aujla" to RobotEmotion.COOL_RAP,                 // (Lifestyle Classic)
        "5 am karan aujla" to RobotEmotion.SAD,
        "fallin apart karan aujla" to RobotEmotion.SAD,
        "take it easy karan aujla" to RobotEmotion.HAPPY_CHILL,

        "intro karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "here and there karan aujla" to RobotEmotion.COOL_RAP,
        "click that b kickin it karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "it aint legal karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "boli karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "itz a hustle karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "ford karan aujla" to RobotEmotion.COOL_RAP,                  // (Cruising)
        "addi sunni karan aujla" to RobotEmotion.SAD,
        "sharab karan aujla" to RobotEmotion.HAPPY_CHILL,             // (Drink/Chill)
        "feel the flava karan aujla" to RobotEmotion.COOL_RAP,
        "80 degrees karan aujla" to RobotEmotion.COOL_RAP,
        "ask about me karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "chu gon do karan aujla" to RobotEmotion.COOL_RAP,
        "addictive karan aujla" to RobotEmotion.ROMANTIC_HAPPY,
        "bhalwani gedi karan aujla" to RobotEmotion.HAPPY_CHILL,

        "dont look karan aujla" to RobotEmotion.AGGRESSIVE_RAP,      // (Intense)
        "chitta kurta karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "same beef karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "no need karan aujla" to RobotEmotion.HAPPY_CHILL,            // (Relaxed)
        "hint karan aujla" to RobotEmotion.COOL_RAP,                  // (Cool/Chill)
        "sheikh karan aujla" to RobotEmotion.COOL_RAP,
        "mexico karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "chithiyaan karan aujla" to RobotEmotion.SAD,                 // (Sad & Slow)
        "dont worry karan aujla" to RobotEmotion.HAPPY_CHILL,        // (Motivational/Relaxed)
        "jhanjar karan aujla" to RobotEmotion.ROMANTIC_HAPPY,         // (Smiling Love)
        "rim vs jhanjar karan aujla" to RobotEmotion.HAPPY_CHILL,
        "kyon karan aujla" to RobotEmotion.SAD,
        "soch karan aujla" to RobotEmotion.SAD,
        "kya baat aa karan aujla" to RobotEmotion.ROMANTIC_HAPPY,
        "red eyes karan aujla" to RobotEmotion.COOL_RAP,
        "hukam karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "hair karan aujla" to RobotEmotion.ROMANTIC_HAPPY,
        "2am karan aujla" to RobotEmotion.SAD,
        "letem play karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "han haige aa karan aujla" to RobotEmotion.ROMANTIC_HAPPY,
        "facts karan aujla" to RobotEmotion.COOL_RAP,
        "on top karan aujla" to RobotEmotion.COOL_RAP,
        "way ahead karan aujla" to RobotEmotion.COOL_RAP,
        "na na na karan aujla" to RobotEmotion.COOL_RAP,
        "unity karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "sikander karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "jatt da muqabla karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "pov karan aujla" to RobotEmotion.COOL_RAP,
        "laut aana karan aujla" to RobotEmotion.SAD,
        "players karan aujla" to RobotEmotion.HAPPY_ENERGETIC,        // (Dancing!)
        "white brown black karan aujla" to RobotEmotion.COOL_RAP,
        "sheesha karan aujla" to RobotEmotion.ROMANTIC_HAPPY,

        "gun shot karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "6 bandeh karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "weak point karan aujla" to RobotEmotion.SAD,
        "yaarian ch medal karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "alcohol 2 karan aujla" to RobotEmotion.HAPPY_ENERGETIC,      // (Party!)
        "property of punjab karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "shit talk karan aujla" to RobotEmotion.AGGRESSIVE_RAP,
        "approach karan aujla" to RobotEmotion.COOL_RAP,
        "manja karan aujla" to RobotEmotion.HAPPY_CHILL,
        "guilty karan aujla" to RobotEmotion.SAD,
        "little bit karan aujla" to RobotEmotion.ROMANTIC_HAPPY,

        "players badshah" to RobotEmotion.HAPPY_ENERGETIC,            // (Dancing!)
        "god damn badshah" to RobotEmotion.HAPPY_ENERGETIC,           // (Dancing!)
        "scared money diljit dosanjh" to RobotEmotion.COOL_RAP,
        "ask them gippy grewal" to RobotEmotion.COOL_RAP,
        "tru talk jassie gill" to RobotEmotion.COOL_RAP,
        "sheesha jassie gill" to RobotEmotion.ROMANTIC_HAPPY,
        "snake deep jandu" to RobotEmotion.AGGRESSIVE_RAP,
        "aukaat deep jandu" to RobotEmotion.AGGRESSIVE_RAP,
        "red light deep jandu" to RobotEmotion.AGGRESSIVE_RAP,
        "underestimate deep jandu" to RobotEmotion.AGGRESSIVE_RAP,
        "minister deep jandu" to RobotEmotion.COOL_RAP,
        "bombay to punjab deep jandu" to RobotEmotion.AGGRESSIVE_RAP,
        "my name deep jandu" to RobotEmotion.COOL_RAP,
        "dont tell me dilpreet dhillon" to RobotEmotion.AGGRESSIVE_RAP,
        "jatt te jawani dilpreet dhillon" to RobotEmotion.AGGRESSIVE_RAP,
        "yaar graribaaz dilpreet dhillon" to RobotEmotion.HAPPY_CHILL,
        "gunda touch elly mangat" to RobotEmotion.AGGRESSIVE_RAP,
        "snitch elly mangat" to RobotEmotion.AGGRESSIVE_RAP,
        "bhang weed elly mangat" to RobotEmotion.AGGRESSIVE_RAP,
        "supply gurjas sidhu" to RobotEmotion.AGGRESSIVE_RAP,
        "pcr gurjas sidhu" to RobotEmotion.AGGRESSIVE_RAP,
        "dere vale yaar honey gill" to RobotEmotion.SAD,
        "few days amantej hundal" to RobotEmotion.ROMANTIC_HAPPY,
        "hard to get subaig singh" to RobotEmotion.HAPPY_CHILL,
        "burnout dj flow" to RobotEmotion.SAD,
        "madam ji tushar" to RobotEmotion.HAPPY_CHILL,
        "soch intense" to RobotEmotion.SAD,
        "ek din bohemia" to RobotEmotion.AGGRESSIVE_RAP,
        "these days bohemia" to RobotEmotion.SAD,

        "sachay loki talwiinder" to RobotEmotion.SAD,                 // (Dec 2025 - Deep Sadness)
        "tenu zyada mohabbat talwiinder" to RobotEmotion.ROMANTIC_SLOW,// (Bollywood Love Song)
        "kaatilana talwiinder" to RobotEmotion.COOL_RAP,              // (Flex/Style)
        "kaaliyan raatan talwiinder" to RobotEmotion.SAD,             // (Dark Vibe)
        "how talwiinder" to RobotEmotion.SAD,                         // (Emotional)
        "panchii talwiinder" to RobotEmotion.HAPPY_CHILL,             // (Summer Vibe - Relaxed)
        "nakhre talwiinder" to RobotEmotion.COOL_RAP,              // (Smooth)
        "pal pal talwiinder" to RobotEmotion.SAD,          // (Love Hit)
        "flowers talwiinder" to RobotEmotion.HAPPY_ENERGETIC,         // (Upbeat/Fast - Dancing!)
        "haseen talwiinder" to RobotEmotion.SAD,                 // (Viral Hit - Smooth)
        "haseen remix talwiinder" to RobotEmotion.SAD,    // (Club Mix - Dancing!)
        "haal puchte hai talwiinder" to RobotEmotion.SAD,

        "dil wich talwiinder" to RobotEmotion.SAD,
        "tera chera talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "mera talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "say something talwiinder" to RobotEmotion.COOL_RAP,
        "dil di gall talwiinder" to RobotEmotion.SAD,
        "lukake talwiinder" to RobotEmotion.ROMANTIC_HAPPY,
        "kashni talwiinder" to RobotEmotion.SAD,
        "akhiyan talwiinder" to RobotEmotion.SAD,
        "sajjna talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "tere bin talwiinder" to RobotEmotion.SAD,                    // (Heartbreak)
        "need talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "kehri duniya talwiinder" to RobotEmotion.SAD,
        "tunnel vision talwiinder" to RobotEmotion.COOL_RAP,

        "tu talwiinder" to RobotEmotion.COOL_RAP,                     // (Vibe)
        "khayaal talwiinder" to RobotEmotion.ROMANTIC_SLOW,           // (Signature Slow Love)
        "dhundhala talwiinder" to RobotEmotion.COOL_RAP,              // (Vibe Anthem)
        "gallan 4 talwiinder" to RobotEmotion.COOL_RAP,               // (The OG Vibe)
        "gallan 4 remake talwiinder" to RobotEmotion.COOL_RAP,
        "unforgettable talwiinder" to RobotEmotion.COOL_RAP,
        "tera saath talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "kammo ji talwiinder" to RobotEmotion.HAPPY_ENERGETIC,        // (Fun/Upbeat - Dancing!)
        "jaqeen talwiinder" to RobotEmotion.SAD,
        "dila talwiinder" to RobotEmotion.SAD,
        "kitaab talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "anonymous talwiinder" to RobotEmotion.COOL_RAP,
        "conversation talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "gal kardi talwiinder" to RobotEmotion.HAPPY_ENERGETIC,       // (Dance Track)
        "heer talwiinder" to RobotEmotion.HAPPY_CHILL,                // (Fusion Vibe)
        "injh na kar talwiinder" to RobotEmotion.SAD,
        "save me talwiinder" to RobotEmotion.SAD,
        "agg banke talwiinder" to RobotEmotion.AGGRESSIVE_RAP,        // (Intense!)
        "gaah talwiinder" to RobotEmotion.AGGRESSIVE_RAP,             // (Intense!)
        "khoya talwiinder" to RobotEmotion.SAD,
        "soch talwiinder" to RobotEmotion.SAD,
        "dil mera talwiinder" to RobotEmotion.SAD,
        "baarish da mausam talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "sweet talk talwiinder" to RobotEmotion.HAPPY_ENERGETIC,      // (Upbeat)
        "someone like me talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "andaaz talwiinder" to RobotEmotion.COOL_RAP,
        "her talwiinder" to RobotEmotion.ROMANTIC_SLOW,
        "jaan talwiinder" to RobotEmotion.ROMANTIC_HAPPY,
        "dil te dimag talwiinder" to RobotEmotion.SAD,
        "gaani talwiinder" to RobotEmotion.COOL_RAP,
        "dnd talwiinder" to RobotEmotion.AGGRESSIVE_RAP,              // (Fired Up!)
        "aja talwiinder" to RobotEmotion.COOL_RAP,

        "nasha talwiinder" to RobotEmotion.COOL_RAP,                  // (Dark Vibe)
        "haal talwiinder" to RobotEmotion.SAD,                        // (Very Emotional)
        "your eyes talwiinder" to RobotEmotion.ROMANTIC_SLOW,         // (Slow Burn)

        "high on me yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC, // (Party Hit!)
        "wishes hasan raheem" to RobotEmotion.ROMANTIC_SLOW,
        "broken hearts imran khan" to RobotEmotion.COOL_RAP,
        "the way you look noor chahal" to RobotEmotion.ROMANTIC_SLOW,
        "void sxr" to RobotEmotion.SAD,
        "pray on it remix rhea raj" to RobotEmotion.HAPPY_ENERGETIC,    // (Dance Remix)
        "baazi supreme sidhu" to RobotEmotion.COOL_RAP,
        "gallan teri baaton mein aisa uljha jiya" to RobotEmotion.COOL_RAP, // (Bollywood Vibe)

        "one bottle down yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,  // (2015 - Verified Hit)
        "dheere dheere yo yo honey singh" to RobotEmotion.ROMANTIC_SLOW,      // (2015 - Record Breaker)
        "birthday bash yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,    // (2015 - Dilliwali Zaalim Girlfriend)
        "blue eyes yo yo honey singh" to RobotEmotion.ROMANTIC_HAPPY,         // (2013 - Blockbuster)
        "desi kalakaar yo yo honey singh" to RobotEmotion.COOL_RAP,           // (2014 - Storytelling)
        "love dose yo yo honey singh" to RobotEmotion.ROMANTIC_HAPPY,         // (2014 - Flirty)
        "brown rang yo yo honey singh" to RobotEmotion.COOL_RAP,              // (2011 - The Classic)
        "lungi dance yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,      // (2013 - Chennai Express)
        "party all night yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,  // (2013 - Boss)
        "chaar botal vodka yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,// (2014 - Ragini MMS 2)
        "sunny sunny yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,      // (2014 - Yaariyan)
        "manali trance yo yo honey singh" to RobotEmotion.HAPPY_CHILL,        // (2014 - Trippy Vibe)
        "bring me back yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP,     // (2013 - Spoken Word)
        "issey kehte hain hip hop yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP, // (2014 - Anthem)
        "chal mere ghar yo yo honey singh" to RobotEmotion.HAPPY_CHILL,       // (2014 - Vibe)
        "one thousand miles yo yo honey singh" to RobotEmotion.ROMANTIC_SLOW, // (2014 - Slow Love)
        "alcoholic yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,        // (2014 - The Shaukeens)
        "yaar naa miley yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP,    // (2014 - Kick Devil Song)
        "aata majhi satakli yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP,// (2014 - Singham Returns)
        "aao raja yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP,          // (2015 - Gabbar Is Back)
        "aankhon aankhon yo yo honey singh" to RobotEmotion.ROMANTIC_HAPPY,   // (2015 - Bhaag Johnny)
        "char shanivaar yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,   // (2015 - All Is Well)
        "supernatural yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,     // (2014 - Bhoothnath Returns)
        "party with the bhoothnath yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,
        "abcd yaariyan yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,    // (Yaariyan)
        "kikli kalerdi yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,
        "boss yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP,
        "horn ok please yo yo honey singh" to RobotEmotion.COOL_RAP,
        "rani tu mein raja yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,
        "lonely yo yo honey singh" to RobotEmotion.SAD,
        "main sharabi yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,
        "stardom yo yo honey singh" to RobotEmotion.COOL_RAP,                 // (Desi Kalakaar Album)
        "i am your dj yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,     // (Desi Kalakaar Album)
        "daftar ki girl yo yo honey singh" to RobotEmotion.COOL_RAP,          // (Desi Kalakaar Album)

        "angreji beat yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,     // (ft. Gippy Grewal)
        "dope shope yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,       // (ft. Deep Money)
        "gabru yo yo honey singh" to RobotEmotion.COOL_RAP,                   // (ft. J Star)
        "goliyan yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP,           // (ft. Diljit Dosanjh)
        "get up jawani yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,    // (ft. Badshah)
        "beautiful yo yo honey singh" to RobotEmotion.ROMANTIC_HAPPY,         // (ft. Malkit Singh)
        "mujhe peene do yo yo honey singh" to RobotEmotion.SAD,               // (Drinking/Sad)
        "yaaran di yaari yo yo honey singh" to RobotEmotion.COOL_RAP,
        "sambhle yo yo honey singh" to RobotEmotion.HAPPY_CHILL,
        "head banger yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP,
        "taxi yo yo honey singh" to RobotEmotion.COOL_RAP,
        "yaad yo yo honey singh" to RobotEmotion.SAD,
        "aashke yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,
        "garaari yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP,

        "lakk 28 kudi da yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,  // (Massive Hit - 2011)
        "panga diljit dosanjh" to RobotEmotion.AGGRESSIVE_RAP,                // (2009)
        "rubaru diljit dosanjh" to RobotEmotion.ROMANTIC_SLOW,
        "khadka diljit dosanjh" to RobotEmotion.AGGRESSIVE_RAP,
        "15 saal diljit dosanjh" to RobotEmotion.HAPPY_CHILL,                 // (2011 - Urban Pendu Leak)
        "dil nachda diljit dosanjh" to RobotEmotion.HAPPY_ENERGETIC,
        "los angeles diljit dosanjh" to RobotEmotion.COOL_RAP,
        "bhagat singh diljit dosanjh" to RobotEmotion.AGGRESSIVE_RAP,         // (Patriotic)
        "dance with me diljit dosanjh" to RobotEmotion.HAPPY_ENERGETIC,

        "hummer nishawn bhullar" to RobotEmotion.COOL_RAP,                    // (Massive Car Anthem)
        "thokda reha nishawn bhullar" to RobotEmotion.AGGRESSIVE_RAP,         // (Heavy)
        "siftan money aujla" to RobotEmotion.COOL_RAP,                        // (Cult Hit)
        "london money aujla" to RobotEmotion.HAPPY_CHILL,                     // (Vibe)
        "goli ks makhan" to RobotEmotion.AGGRESSIVE_RAP,                      // (Gangster Rap Classic)
        "dhoor manak e" to RobotEmotion.COOL_RAP,                             // (Manak-E's Biggest Hit - 2009)
        "mood kharab raja baath" to RobotEmotion.AGGRESSIVE_RAP,              // (Attitude)
        "pilli pilli raja baath" to RobotEmotion.HAPPY_CHILL,                 // (Fun Vibe)
        "banda marna inderjit nikku" to RobotEmotion.AGGRESSIVE_RAP,          // (Heavy)
        "zanjeer karan jasbir" to RobotEmotion.AGGRESSIVE_RAP,                // (Rare)

        "haye mera dil alfaaz" to RobotEmotion.ROMANTIC_SLOW,                 // (Signature Heartbreak)
        "bebo alfaaz" to RobotEmotion.HAPPY_ENERGETIC,                        // (Club Hit)
        "yaar bathere alfaaz" to RobotEmotion.SAD,
        "rikshaw alfaaz" to RobotEmotion.COOL_RAP,
        "eid alfaaz" to RobotEmotion.SAD,
        "sire di naar alfaaz" to RobotEmotion.ROMANTIC_HAPPY,

        "high heels yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,       // (ft. Jaz Dhami - 2012)
        "this party getting hot yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC, // (ft. Jazzy B)
        "shartan jazzy b" to RobotEmotion.COOL_RAP,
        "chad gayi gippy grewal" to RobotEmotion.HAPPY_ENERGETIC,             // (Verified Spelling)
        "flower gippy grewal" to RobotEmotion.ROMANTIC_HAPPY,
        "achko machko yo yo honey singh" to RobotEmotion.HAPPY_ENERGETIC,
        "glassy ashok masti" to RobotEmotion.HAPPY_ENERGETIC,                 // (The Original Glassy)
        "choot vol 1 yo yo honey singh" to RobotEmotion.AGGRESSIVE_RAP,       // (Explicit Classic)
        "khol botal badshah" to RobotEmotion.HAPPY_ENERGETIC,                 // (Pre-2012)
        "begani naar yo yo honey singh" to RobotEmotion.COOL_RAP,
        "condom yo yo honey singh" to RobotEmotion.HAPPY_CHILL,

        "farebi chaar diwaari" to RobotEmotion.AGGRESSIVE_RAP,
        "banda kaam ka chaar diwaari" to RobotEmotion.ROMANTIC_HAPPY,
        "thehra chaar diwaari" to RobotEmotion.SAD,
        "lovesexdhoka chaar diwaari" to RobotEmotion.HAPPY_ENERGETIC,
        "mera saman kahan hai chaar diwaari" to RobotEmotion.AGGRESSIVE_RAP,
        "kya chaar diwaari" to RobotEmotion.AGGRESSIVE_RAP,
        "aankh band chaar diwaari" to RobotEmotion.AGGRESSIVE_RAP,
        "mitti chaar diwaari" to RobotEmotion.AGGRESSIVE_RAP,
        "jhaag chaar diwaari" to RobotEmotion.COOL_RAP,
        "barood chaar diwaari" to RobotEmotion.AGGRESSIVE_RAP,
        "violence chaar diwaari" to RobotEmotion.AGGRESSIVE_RAP,
        "garam chaar diwaari" to RobotEmotion.AGGRESSIVE_RAP,
        "enjaay chaar diwaari" to RobotEmotion.COOL_RAP,
        "roshni bharg" to RobotEmotion.COOL_RAP,
        "kaun mera chaar diwaari" to RobotEmotion.COOL_RAP,
        "rang chaar diwaari" to RobotEmotion.SAD,
        "identity theft yashraj" to RobotEmotion.AGGRESSIVE_RAP,
        "mahalla arpit bala" to RobotEmotion.COOL_RAP,
        "bandar arpit bala" to RobotEmotion.HAPPY_ENERGETIC,
        "mujhko mila karun" to RobotEmotion.COOL_RAP,
        "bhool ja nanku" to RobotEmotion.AGGRESSIVE_RAP,

        "attention newjeans" to RobotEmotion.COOL_RAP,            // (Stylish/Cool Vibe)
        "hype boy newjeans" to RobotEmotion.HAPPY_ENERGETIC,      // (Pure Happiness/Dance)
        "cookie newjeans" to RobotEmotion.COOL_RAP,               // (R&B Flow)
        "hurt newjeans" to RobotEmotion.SAD,                      // (Slow Ballad)
        "omg newjeans" to RobotEmotion.HAPPY_ENERGETIC,           // (Bouncy/Upbeat)
        "ditto newjeans" to RobotEmotion.SAD,                     // (Melancholic/Nostalgic Vibe)
        "super shy newjeans" to RobotEmotion.HAPPY_ENERGETIC,     // (Fast Jersey Club)
        "eta newjeans" to RobotEmotion.HAPPY_ENERGETIC,           // (Fast Paced)
        "new jeans newjeans" to RobotEmotion.COOL_RAP,            // (Unique Vibe)
        "cool with you newjeans" to RobotEmotion.ROMANTIC_SLOW,   // (Dreamy/Vocal)
        "get up newjeans" to RobotEmotion.SAD,                    // (Short R&B Ballad)
        "asap newjeans" to RobotEmotion.HAPPY_CHILL,
        "how sweet newjeans" to RobotEmotion.HAPPY_ENERGETIC,     // (Miami Bass = DANCING!)
        "bubble gum newjeans" to RobotEmotion.HAPPY_CHILL,        // (City Pop/Relaxed)
        "supernatural newjeans" to RobotEmotion.HAPPY_ENERGETIC,  // (New Jack Swing - Dance)
        "right now newjeans" to RobotEmotion.HAPPY_CHILL,         // (Smooth Drum & Bass)
        "gods newjeans" to RobotEmotion.AGGRESSIVE_RAP,
        "our night is more beautiful than your day newjeans" to RobotEmotion.HAPPY_CHILL,
        "beautiful restriction newjeans" to RobotEmotion.SAD,
        "zero newjeans" to RobotEmotion.HAPPY_ENERGETIC,

        "all the same the debut dream academy" to RobotEmotion.SAD,
        "all the same katseye" to RobotEmotion.SAD,
        "dirty water the debut dream academy" to RobotEmotion.HAPPY_ENERGETIC,
        "dirty water katseye" to RobotEmotion.HAPPY_ENERGETIC,
        "girls dont like the debut dream academy" to RobotEmotion.COOL_RAP,
        "girls dont like katseye" to RobotEmotion.COOL_RAP,
        "love myself the debut dream academy" to RobotEmotion.HAPPY_CHILL,
        "love myself katseye" to RobotEmotion.HAPPY_CHILL,
        "debut katseye" to RobotEmotion.HAPPY_ENERGETIC,
        "touch katseye" to RobotEmotion.ROMANTIC_HAPPY,
        "my way katseye" to RobotEmotion.HAPPY_CHILL,
        "im pretty katseye" to RobotEmotion.HAPPY_ENERGETIC,
        "tonight i might katseye" to RobotEmotion.HAPPY_ENERGETIC,
        "gnarly katseye" to RobotEmotion.AGGRESSIVE_RAP,
        "gabriela katseye" to RobotEmotion.HAPPY_ENERGETIC,
        "gameboy katseye" to RobotEmotion.HAPPY_ENERGETIC,
        "mean girls katseye" to RobotEmotion.COOL_RAP,
        "mia katseye" to RobotEmotion.AGGRESSIVE_RAP,
        "internet girl katseye" to RobotEmotion.HAPPY_ENERGETIC,
        "flame katseye" to RobotEmotion.AGGRESSIVE_RAP,
        "monster high fright song monster high" to RobotEmotion.HAPPY_ENERGETIC,
        "monster high fright song katseye" to RobotEmotion.HAPPY_ENERGETIC,

        "brutal olivia rodrigo" to RobotEmotion.AGGRESSIVE_RAP,
        "traitor olivia rodrigo" to RobotEmotion.SAD,
        "drivers license olivia rodrigo" to RobotEmotion.SAD,
        "1 step forward 3 steps back olivia rodrigo" to RobotEmotion.SAD,
        "deja vu olivia rodrigo" to RobotEmotion.SAD,
        "good 4 u olivia rodrigo" to RobotEmotion.AGGRESSIVE_RAP,
        "enough for you olivia rodrigo" to RobotEmotion.SAD,
        "happier olivia rodrigo" to RobotEmotion.SAD,
        "jealousy jealousy olivia rodrigo" to RobotEmotion.AGGRESSIVE_RAP,
        "favorite crime olivia rodrigo" to RobotEmotion.SAD,
        "hope ur ok olivia rodrigo" to RobotEmotion.HAPPY_CHILL,
        "the rose song olivia rodrigo" to RobotEmotion.SAD,
        "the rose song cast of high school musical the musical the series" to RobotEmotion.SAD,
        "logical olivia rodrigo" to RobotEmotion.SAD,
        "1 step forward 3 steps back live olivia rodrigo" to RobotEmotion.SAD,
        "traitor live olivia rodrigo" to RobotEmotion.SAD,

        "so high doja cat" to RobotEmotion.HAPPY_CHILL,
        "candy doja cat" to RobotEmotion.HAPPY_CHILL,
        "go to town doja cat" to RobotEmotion.HAPPY_ENERGETIC,
        "cookie jar doja cat" to RobotEmotion.HAPPY_ENERGETIC,
        "morning light doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "wine pon you doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "tia tamera doja cat" to RobotEmotion.AGGRESSIVE_RAP,
        "juicy doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "mooo doja cat" to RobotEmotion.HAPPY_CHILL,
        "say so doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "like that doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "streets doja cat" to RobotEmotion.SAD,
        "cyber sex doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "rules doja cat" to RobotEmotion.COOL_RAP,
        "bottom bitch doja cat" to RobotEmotion.AGGRESSIVE_RAP,
        "win doja cat" to RobotEmotion.HAPPY_ENERGETIC,
        "talk dirty doja cat" to RobotEmotion.COOL_RAP,
        "addiction doja cat" to RobotEmotion.HAPPY_CHILL,
        "shine doja cat" to RobotEmotion.HAPPY_CHILL,
        "fancy doja cat" to RobotEmotion.HAPPY_ENERGETIC,
        "woman doja cat" to RobotEmotion.HAPPY_ENERGETIC,
        "kiss me more doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "need to know doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "get into it doja cat" to RobotEmotion.AGGRESSIVE_RAP,
        "aint shit doja cat" to RobotEmotion.COOL_RAP,
        "you right doja cat" to RobotEmotion.HAPPY_CHILL,
        "options doja cat" to RobotEmotion.COOL_RAP,
        "naked doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "love to dream doja cat" to RobotEmotion.SAD,
        "been like this doja cat" to RobotEmotion.HAPPY_CHILL,
        "wild beach doja cat" to RobotEmotion.HAPPY_CHILL,
        "i dont do drugs doja cat" to RobotEmotion.HAPPY_CHILL,
        "payday doja cat" to RobotEmotion.HAPPY_ENERGETIC,
        "paint the town red doja cat" to RobotEmotion.AGGRESSIVE_RAP,
        "agora hills doja cat" to RobotEmotion.ROMANTIC_HAPPY,
        "attention doja cat" to RobotEmotion.COOL_RAP,
        "demons doja cat" to RobotEmotion.AGGRESSIVE_RAP,
        "masc doja cat" to RobotEmotion.SAD,
        "gun doja cat" to RobotEmotion.AGGRESSIVE_RAP,
        "shutcho doja cat" to RobotEmotion.AGGRESSIVE_RAP,
        "boss bitch doja cat" to RobotEmotion.AGGRESSIVE_RAP,
        "vegas doja cat" to RobotEmotion.COOL_RAP,
        "freak doja cat" to RobotEmotion.COOL_RAP,

        "munch ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "bikini bottom ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "name of love ice spice" to RobotEmotion.HAPPY_CHILL,
        "no clarity ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "in ha mood ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "princess diana ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "gangsta boo ice spice" to RobotEmotion.COOL_RAP,
        "boys a liar pt 2 pinkpantheress" to RobotEmotion.HAPPY_ENERGETIC,
        "boys a liar pt 2 ice spice" to RobotEmotion.HAPPY_ENERGETIC,
        "karma taylor swift" to RobotEmotion.HAPPY_ENERGETIC,
        "actin a smoochie ice spice" to RobotEmotion.COOL_RAP,
        "be a lady ice spice" to RobotEmotion.COOL_RAP,
        "once upon a time ice spice" to RobotEmotion.HAPPY_CHILL,
        "deli ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "barbie world ice spice" to RobotEmotion.HAPPY_ENERGETIC,
        "barbie world nicki minaj" to RobotEmotion.HAPPY_ENERGETIC,
        "how high ice spice" to RobotEmotion.COOL_RAP,
        "did it first ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "think u the shit ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "think u the shit fart ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "phat butt ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        "gimme a light ice spice" to RobotEmotion.HAPPY_ENERGETIC,
        "fisherrr cash cobain" to RobotEmotion.COOL_RAP,
        "oh shhh ice spice" to RobotEmotion.AGGRESSIVE_RAP,
        )

    suspend fun getEmotionForSong(songName: String, artistName: String): RobotEmotion {
        // Generate the "Clean Key
        val cleanKey = generateCleanKey(songName, artistName)

        // 2. Check Preloaded with the CLEAN key
        preloadedEmotions[cleanKey]?.let {
            lastAudioFeatures = null
            GlobalDebug.lastError = ""
            return it
        }

        // 3. Check Cache
        val cachedEmotion = getCachedEmotion(cleanKey)
        if (cachedEmotion != null) {
            //Retrieve the features from cache too
            GlobalDebug.lastError = ""
            return cachedEmotion
        }

        // 4. Call API (Send original names to API for accuracy)
        if (canMakeAPICall()) {
            val features = rapidAPI.getAudioFeatures(songName, artistName)
            if (features != null) {
                lastAudioFeatures = features
                val emotion = expressionEngine.analyzeEmotion(features)
                // Cache BOTH emotion and features using CLEAN key
                cacheData(cleanKey, emotion, features)
                incrementAPICallCount()
                return emotion
            } else {
                lastAudioFeatures = null
                return RobotEmotion.NEUTRAL
            }
        }
        GlobalDebug.lastError = "Daily Limit Reached (0/3)"
        return RobotEmotion.NEUTRAL
    }

    private fun generateCleanKey(songName: String, artistName: String): String {
        //CLEAN THE ARTIST (Take only the primary artist)
        // We split by comma (,) or ampersand (&) and take the first part.
        var cleanArtist = artistName.lowercase()
        if (cleanArtist.contains(",")) {
            cleanArtist = cleanArtist.split(",")[0]
        }
        if (cleanArtist.contains("&")) {
            cleanArtist = cleanArtist.split("&")[0]
        }
        cleanArtist = cleanArtist.trim()

        // CLEAN THE SONG TITLE
        // We remove everything inside () and []
        var cleanSong = songName.lowercase()
        cleanSong = cleanSong.replace(Regex("\\(.*?\\)"), "") // Remove (feat. X)
        cleanSong = cleanSong.replace(Regex("\\[.*?\\]"), "") // Remove [Remix]

        // Handle hyphens with spaces (preserves "anti-hero")
        if (cleanSong.contains(" - ")) cleanSong = cleanSong.substringBefore(" - ").trim()

        // Handle common variations that don't use parentheses or hyphens
        val variations = listOf(
            " slowed reverb", " slowed and reverb", " slowed + reverb",
            " sped up", " nightcore", " 8d audio", " bass boosted",
            " clean version", " explicit version", " radio edit",
            " acoustic version", " live version", " remastered"
        )

        for (variation in variations) {
            if (cleanSong.contains(variation)) {
                cleanSong = cleanSong.replace(variation, "").trim()
            }
        }

        //REMOVE PUNCTUATION (Apostrophes, dashes, etc.)
        // "God's Plan" -> "gods plan"
        cleanSong = cleanSong.replace(Regex("[^a-z0-9 ]"), "").trim()
        cleanArtist = cleanArtist.replace(Regex("[^a-z0-9 ]"), "").trim()

        //COMBINE
        val finalKey = "$cleanSong $cleanArtist"
        // Remove double spaces just in case
        return finalKey.replace("  ", " ")
    }

    // --- HELPER FUNCTIONS ---

    private fun getCachedEmotion(songKey: String): RobotEmotion? {
        val prefs = context.getSharedPreferences("song_cache", ComponentActivity.MODE_PRIVATE)
        val emotionName = prefs.getString("emotion_$songKey", null)
        return emotionName?.let { try { RobotEmotion.valueOf(it) } catch (e: Exception) { null } }
    }

    private fun cacheData(songKey: String, emotion: RobotEmotion, features: AudioFeatures) {
        val prefs = context.getSharedPreferences("song_cache", ComponentActivity.MODE_PRIVATE)
        val editor = prefs.edit()

        // Save Emotion
        editor.putString("emotion_$songKey", emotion.name)

        editor.apply()
    }

    // ... (Keep your existing canMakeAPICall, incrementAPICallCount, getAPICallsRemaining, etc.) ...
    private fun canMakeAPICall(): Boolean {
        val prefs = context.getSharedPreferences("api_usage", ComponentActivity.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString("last_date", "")
        return if (lastDate == today) prefs.getInt("calls_today", 0) < 3 else true
    }

    private fun incrementAPICallCount() {
        val prefs = context.getSharedPreferences("api_usage", ComponentActivity.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val callsToday = if (prefs.getString("last_date", "") == today) prefs.getInt("calls_today", 0) else 0
        prefs.edit().putString("last_date", today).putInt("calls_today", callsToday + 1).apply()
    }

    fun clearAllCache() {
        context.getSharedPreferences("song_cache", ComponentActivity.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("api_usage", ComponentActivity.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("robot_info", ComponentActivity.MODE_PRIVATE).edit().clear().apply()
        GlobalDebug.lastError = "Cache Reset. Try again."
    }

    fun getLastAudioFeatures(): AudioFeatures? = lastAudioFeatures
}

// --- REAL MUSIC DETECTION SERVICE ---
class MediaSessionMusicService(private val context: Context) {

    fun getCurrentlyPlaying(): CurrentlyPlaying? {
        return try {
            val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

            val componentName = ComponentName(context, NotificationService::class.java)
            val activeSessions = mediaSessionManager.getActiveSessions(componentName)

            for (session in activeSessions) {
                val metadata = session.metadata
                val playbackState = session.playbackState

                if (metadata != null) {
                    val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                    val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)

                    if (!title.isNullOrEmpty() && !artist.isNullOrEmpty()) {
                        val track = Track("media_session", title, artist, album ?: "Unknown Album")
                        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

                        return CurrentlyPlaying(track, isPlaying ?: false)
                    }
                }
            }

            GlobalDebug.lastError = "❌ No active media sessions found"
            null

        } catch (e: SecurityException) {
            GlobalDebug.lastError = "❌ Permission denied: Enable notification access in Settings"
            null
        } catch (e: Exception) {
            GlobalDebug.lastError = "❌ MediaSession error: ${e.message}"
            null
        }
    }

    fun hasNotificationPermission(): Boolean {
        return try {
            val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val componentName = ComponentName(context, NotificationService::class.java)
            mediaSessionManager.getActiveSessions(componentName)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class RobotExpressionEngine {
    fun analyzeEmotion(features: AudioFeatures): RobotEmotion {
        return when {
            //rap
            features.speechiness > 25f -> {
                if (features.energy > 60f) {
                    RobotEmotion.AGGRESSIVE_RAP
                }
                else {
                    RobotEmotion.COOL_RAP
                }
            }

            //sad done
            features.speechiness < 12f && features.valence < 40f -> {
                RobotEmotion.SAD
            }

            //aggressive happy done
            features.valence >= 40f && (features.energy > 60f || features.danceability > 50f) -> {
                RobotEmotion.HAPPY_ENERGETIC
            }

            //slow romance
            features.energy < 60f && features.acousticness > 45f && features.valence > 35f -> {
                RobotEmotion.ROMANTIC_SLOW
            }

            //sexy romance
            features.energy < 60f && features.danceability > 60f && features.valence < 70f -> {
                RobotEmotion.ROMANTIC_HAPPY
            }

            //calm happy
            features.valence > 40f && features.energy < 60f -> {
                RobotEmotion.HAPPY_CHILL
            }

            else -> RobotEmotion.NEUTRAL
        }
    }
    fun getEmotionDescription(emotion: RobotEmotion): String = when(emotion) {
        RobotEmotion.HAPPY_ENERGETIC -> "HAPPY AND ENERGETIC"
        RobotEmotion.HAPPY_CHILL -> "HAPPY AND CHILL"
        RobotEmotion.COOL_RAP -> "COOL AND CHILL"
        RobotEmotion.AGGRESSIVE_RAP -> "AGGRESSIVE AND ANGRY"
        RobotEmotion.SAD -> "SAD AND SLOW"
        RobotEmotion.ROMANTIC_SLOW -> "ROMANTIC AND SLOW"
        RobotEmotion.ROMANTIC_HAPPY -> "ROMANTIC AND HAPPY"
        RobotEmotion.NEUTRAL -> "neutral"
    }
}

// --- MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {
    private lateinit var mediaSessionService: MediaSessionMusicService
    private lateinit var smartAnalyzer: SmartMusicAnalyzer
    private lateinit var bleHelper: BleHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        mediaSessionService = MediaSessionMusicService(this)
        smartAnalyzer = SmartMusicAnalyzer(this)
        val prefs = getSharedPreferences("robot_info", MODE_PRIVATE)

        bleHelper = BleHelper(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 1)
        }

        setContent {
            RobotSpotifyAppTheme {
                var isLoggedIn by remember { mutableStateOf(prefs.getBoolean("isLoggedIn", false)) }
                var currentSong by remember { mutableStateOf("No song playing") }
                var currentArtist by remember { mutableStateOf("") }
                var currentEmotion by remember { mutableStateOf(RobotEmotion.NEUTRAL) }
                var audioFeatures by remember { mutableStateOf<AudioFeatures?>(null) }
                var isLoading by remember { mutableStateOf(false) }
                var debugMsg by remember { mutableStateOf("") }

                if (isLoggedIn) {
                    Page2Screen(
                        songName = currentSong,
                        artistName = currentArtist,
                        emotion = currentEmotion,
                        audioFeatures = audioFeatures,
                        emotionDescription = RobotExpressionEngine().getEmotionDescription(currentEmotion),
                        isLoading = isLoading,
                        debugError = debugMsg,
                        onRefresh = {
                            isLoading = true
                            lifecycleScope.launch {
                                try {
                                    // Try to get real currently playing music
                                    val currentlyPlaying = mediaSessionService.getCurrentlyPlaying()

                                    if (currentlyPlaying != null) {
                                        // Real music detected!
                                        currentSong = currentlyPlaying.track.name
                                        currentArtist = currentlyPlaying.track.artistName

                                        val emotion = smartAnalyzer.getEmotionForSong(currentSong, currentArtist)
                                        val features = smartAnalyzer.getLastAudioFeatures()

                                        currentEmotion = emotion
                                        audioFeatures = features
                                        debugMsg = GlobalDebug.lastError

                                        val emotionId = getEmotionId(emotion)
                                        bleHelper.connectAndSend(emotionId)
                                    } else {
                                        // No music detected or permission issue
                                        currentSong = "No music detected"
                                        currentArtist = ""
                                        currentEmotion = RobotEmotion.NEUTRAL
                                        audioFeatures = null
                                        debugMsg = GlobalDebug.lastError

                                        // Check if it's a permission issue
                                        if (!mediaSessionService.hasNotificationPermission()) {
                                            debugMsg = "❌ Grant notification access in Settings → Apps → ${packageManager.getApplicationLabel(applicationInfo)} → Permissions"
                                        }
                                    }
                                } catch (e: Exception) {
                                    debugMsg = "Crash: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        onLogout = {
                            isLoggedIn = false
                            prefs.edit().putBoolean("isLoggedIn", false).apply()
                        },
                        onReset = {
                            smartAnalyzer.clearAllCache()
                            currentSong = "No song playing"
                            currentArtist = ""
                            currentEmotion = RobotEmotion.NEUTRAL
                            audioFeatures = null
                            debugMsg = "Cache Cleared."
                            isLoggedIn = false
                        }
                    )
                } else {
                    LoginScreen {
                        isLoggedIn = true
                        prefs.edit().putBoolean("isLoggedIn", true).apply()
                    }
                }
            }
        }
    }

    private fun getEmotionId(emotion: RobotEmotion): String {
        return when(emotion) {
            RobotEmotion.HAPPY_ENERGETIC -> "1"
            RobotEmotion.HAPPY_CHILL -> "2"
            RobotEmotion.COOL_RAP -> "3"
            RobotEmotion.AGGRESSIVE_RAP -> "4"
            RobotEmotion.SAD -> "5"
            RobotEmotion.ROMANTIC_SLOW -> "6"
            RobotEmotion.ROMANTIC_HAPPY -> "7"
            RobotEmotion.NEUTRAL -> "0"
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4A3B2F)),
        contentAlignment = Alignment.Center) {
        Button(
            onClick = onLoginSuccess,
            modifier = Modifier
                .width(260.dp)
                .height(70.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCEBEB1)),
            shape = RoundedCornerShape(28.dp)) {
            Text("Login<3", color = Color(0xFF3B2F25), fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun Page2Screen(
    songName: String,
    artistName: String,
    emotion: RobotEmotion,
    audioFeatures: AudioFeatures?,
    emotionDescription: String,
    isLoading: Boolean,
    debugError: String,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onReset: () -> Unit
) {
    // 1. Capture the context HERE (at the top)
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCEBEB1))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center){
        Text(
            if (songName == "No song playing") {
                songName
            }
            else {
                "♪ $songName"
            },
            color = Color(0xFF4A3B2F),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        if (artistName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp));
            Text("by $artistName",
                color = Color(0xFF4A3B2F),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            if (isLoading){
                "Analyzing..."
            } else {
                "$emotionDescription"
            },
            color = Color(0xFF4A3B2F),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- ERROR DISPLAY ---
        if (debugError.isNotEmpty() && debugError != "No error yet") {
            Text("DEBUG INFO:",
                color = Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold)

            Text(debugError,
                color = Color.Red,
                fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRefresh,
            enabled = !isLoading,
            modifier = Modifier
                .height(70.dp)
                .width(240.dp),
            colors = ButtonDefaults
                .buttonColors(containerColor = Color(0xFF4A3B2F))) {
            Text(if (isLoading) "Loading..." else "Next Song", color = Color(0xFFCEBEB1), fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .height(70.dp)
                .width(240.dp),
            colors = ButtonDefaults
                .buttonColors(containerColor = Color(0xFF4A3B2F))) {
            Text("Logout", color = Color(0xFFCEBEB1), fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onReset,
            modifier = Modifier
                .height(70.dp)
                .width(240.dp),
            colors = ButtonDefaults
                .buttonColors(containerColor = Color(0xFFB85450))) {
            Text("Reset Cache", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        if (debugError.contains("Grant notification access") || debugError.contains("Permission denied")) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                },
                modifier = Modifier.height(70.dp).width(240.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("OPEN SETTINGS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

class NotificationService : NotificationListenerService() {
    // This class must exist for Android to grant permission.
    // It doesn't need to do anything else.
}