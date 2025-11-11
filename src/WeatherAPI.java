import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Locale;

public class WeatherAPI {

    // **GANTI DENGAN API KEY OPENWEATHERMAP YANG VALID**
    private static final String API_KEY = "64c1ee392ee3eac118a8324485370af1"; 
    
    // PERBAIKAN KRITIS: BASE_URL yang benar untuk data current weather
    private static final String BASE_URL =
    "https://api.openweathermap.org/data/2.5/weather?q=";

    /**
     * Mengambil data cuaca dari OpenWeatherMap.
     * @param city Nama kota yang dicari.
     * @return Objek WeatherData yang berisi informasi cuaca.
     * @throws Exception jika terjadi kegagalan koneksi atau data tidak ditemukan.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static WeatherData getWeather(String city) throws Exception {
        // unit=metric untuk Celcius, lang=id untuk deskripsi cuaca Bahasa Indonesia
        String urlStr = BASE_URL + city + "&appid=" + API_KEY + "&units=metric&lang=id";
    URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(8000);
    conn.setReadTimeout(8000);

    int code = conn.getResponseCode();

        // --- Penanganan Error Koneksi/API ---
        if (code != 200) {
             try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                 StringBuilder errorSb = new StringBuilder();
                 String errorLine;
                 while ((errorLine = errorReader.readLine()) != null) errorSb.append(errorLine);
                 
                 JSONObject errorJson = new JSONObject(errorSb.toString());
                 String message = errorJson.optString("message", "Terjadi kesalahan yang tidak diketahui (Kode: " + code + "). Pastikan nama kota benar.");
                 
                 // Melemparkan exception dengan pesan yang lebih informatif
                 throw new RuntimeException("Gagal ambil data (" + code + "): " + message);
            } catch (Exception parseEx) {
                 throw new RuntimeException("Gagal mengambil data. Response Code: " + code + ". (Mungkin masalah koneksi atau server)");
            }
        }

        // --- Membaca Response Sukses (BufferedReader sudah benar) ---
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        // --- Parsing JSON ---
        JSONObject obj = new JSONObject(sb.toString());

        // Lokasi
        String namaKota = obj.getString("name");
        String negara = obj.getJSONObject("sys").getString("country");

        // Main data
        JSONObject main = obj.getJSONObject("main");
        double suhu = main.getDouble("temp");
        int kelembapan = main.getInt("humidity");
        int tekanan = main.getInt("pressure"); // hPa/mb

        // Cuaca
        JSONArray weatherArray = obj.getJSONArray("weather");
        JSONObject weather = weatherArray.getJSONObject(0);
        String kondisi = weather.getString("description");
        String iconCode = weather.getString("icon");
        
        // Angin
        JSONObject wind = obj.getJSONObject("wind");
        double anginMS = wind.getDouble("speed"); // m/s

        // Kembalikan objek data cuaca
        return new WeatherData(namaKota, negara, suhu, capitalize(kondisi), 
                               kelembapan, tekanan, anginMS, iconCode);
    }
    
    /** Mengubah huruf awal setiap kata menjadi kapital. */
    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] words = s.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase(Locale.ROOT))
                    .append(" ");
            }
        }
        return result.toString().trim();
    }
    
    // --- Kelas Data Model (Inner Class) ---
    public static class WeatherData {
        private final String namaKota;
        private final String negara;
        private final double suhu;
        private final String kondisi;
        private final int kelembapan;
        private final int tekanan;
        private final double anginMS;
        private final String iconCode;

        public WeatherData(String namaKota, String negara, double suhu, String kondisi,
                           int kelembapan, int tekanan, double anginMS, String iconCode) {
            this.namaKota = namaKota;
            this.negara = negara;
            this.suhu = Math.round(suhu * 10) / 10.0; // Bulatkan 1 desimal
            this.kondisi = kondisi;
            this.kelembapan = kelembapan;
            this.tekanan = tekanan;
            this.anginMS = anginMS;
            this.iconCode = iconCode;
        }

        // --- Getters ---
        public double getSuhu() { return suhu; }
        public String getKondisi() { return kondisi; }
        public int getKelembapan() { return kelembapan; }
        public int getTekanan() { return tekanan; } // hPa/mb
        public String getIconCode() { return iconCode; }
        public String getKotaNegara() { return namaKota + ", " + negara; }
        public double getAnginKMH() { return Math.round(anginMS * 3.6); } // Konversi m/s ke km/h
    }
}