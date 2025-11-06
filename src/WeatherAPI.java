import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class WeatherAPI {

    // GANTI DENGAN API KEY OPENWEATHERMAP KAMU
    private static final String API_KEY = "64c1ee392ee3eac118a8324485370af1";
    private static final String BASE_URL = "https://home.openweathermap.org/api_keys";

    public static WeatherData getWeather(String city) throws Exception {
        String urlStr = BASE_URL + city + "&appid=" + API_KEY + "&units=metric&lang=en"; // lang=en agar deskripsi konsisten
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("Gagal ambil data (" + code + "). Cek nama kota / API key.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JSONObject obj = new JSONObject(sb.toString());

        String namaKota = obj.optString("name", city);
        String negara = obj.optJSONObject("sys") != null ? obj.getJSONObject("sys").optString("country", "") : "";
        String kondisiEN = obj.getJSONArray("weather").getJSONObject(0).getString("description"); // ex: scattered clouds
        String iconCode = obj.getJSONArray("weather").getJSONObject(0).getString("icon");        // ex: 03d
        double suhu = obj.getJSONObject("main").getDouble("temp");
        int kelembapan = obj.getJSONObject("main").getInt("humidity");
        int tekanan = obj.getJSONObject("main").getInt("pressure");
        double anginMS = obj.getJSONObject("wind").getDouble("speed"); // m/s

        String kondisiID = toIndonesianTitle(kondisiEN); // sedikit translasi + title case

        return new WeatherData(namaKota, negara, suhu, kondisiID, kelembapan, tekanan, anginMS, iconCode);
    }

    // Translasi ringan EN -> ID + Title Case
    private static String toIndonesianTitle(String en) {
        String s = en.toLowerCase();
        s = s.replace("clear sky", "cerah")
             .replace("few clouds", "cerah berawan")
             .replace("scattered clouds", "berawan")
             .replace("broken clouds", "berawan tebal")
             .replace("overcast clouds", "mendung")
             .replace("light rain", "hujan ringan")
             .replace("moderate rain", "hujan sedang")
             .replace("heavy intensity rain", "hujan lebat")
             .replace("thunderstorm", "badai petir")
             .replace("mist", "kabut");
        // Title Case sederhana
        String[] parts = s.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            out.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return out.toString().trim();
    }

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
            this.suhu = suhu;
            this.kondisi = kondisi;
            this.kelembapan = kelembapan;
            this.tekanan = tekanan;
            this.anginMS = anginMS;
            this.iconCode = iconCode;
        }

        public String getNamaKota() { return namaKota; }
        public String getNegara() { return negara; }
        public double getSuhu() { return suhu; }
        public String getKondisi() { return kondisi; }
        public int getKelembapan() { return kelembapan; }
        public int getTekanan() { return tekanan; }
        public double getAnginMS() { return anginMS; }
        public String getIconCode() { return iconCode; }

        public double getAnginKMH() { return anginMS * 3.6; } // konversi m/s -> km/h
        public String getKotaNegara() {
            return negara == null || negara.isEmpty() ? namaKota : (namaKota + ", " + negara);
        }
    }
}
