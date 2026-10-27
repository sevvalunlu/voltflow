# VoltFlow 

**VoltFlow**, elektrikli scooter filolarını tek bir panelden yönetmek için geliştirilmiş bir **mikromobilite yönetim sistemidir**. Araçların durumunu, batarya seviyelerini, konumlarını ve bakım/şarj geçmişini canlı olarak takip etmeyi sağlar.

Spring Boot ile geliştirilmiş, rol tabanlı giriş sistemine sahip bir web uygulamasıdır.

## Özellikler

- **Dashboard:** Toplam, yayındaki, bakımdaki ve şarjdaki araç sayıları, ortalama batarya seviyesi, toplam bakım maliyeti ve araç durumlarının dağılım grafiği (Chart.js).
- **Acil şarj listesi:** Bataryası %20'nin altına düşen araçlar ayrıca listelenir.
- **Filo yönetimi:** Araç ekleme, düzenleme, silme ve detay görüntüleme. Araçlar duruma (Yayında / Bakımda / Şarjda) ve kritik batarya seviyesine göre filtrelenebilir.
- **Canlı harita:** Yayındaki araçlar İstanbul haritasında duruma göre renklendirilmiş işaretçilerle gösterilir. İşaretçiye tıklayınca araç özeti ve detay sayfası bağlantısı açılır. Yönetici, haritaya tıklayıp bir aracı seçerek aracın konumunu güncelleyebilir.
- **Otomatik operasyon simülasyonu:** Zamanlanmış görevler her dakika çalışır:
  - Yayındaki araçların bataryası yavaş yavaş azalır.
  - Batarya %10'a düştüğünde araç otomatik olarak şarja alınır ve bakım kaydı oluşturulur.
  - Şarjdaki araçların bataryası artar; %100 olunca araç rastgele bir konumla tekrar sahaya sürülür.
- **Bakım ve şarj geçmişi:** Her otomatik ve manuel işlem maliyetiyle birlikte kaydedilir ve araç detay sayfasında listelenir.
- **Bildirimler:** Önemli olaylar (otomatik şarja alma, bakım tamamlanması, şarj tamamlanması) son bildirimler olarak gösterilir.
- **Rol tabanlı yetkilendirme:** Giriş zorunludur. Araç ekleme ve silme işlemleri sadece `ADMIN` rolüne açıktır.
- **Doğrulama:** Seri numarası benzersizliği, batarya aralığı (0–100) ve zorunlu alan kontrolleri yapılır.

## Kullanılan Teknolojiler

| Katman | Teknoloji |
| --- | --- |
| Dil / Çatı | Java 21, Spring Boot 3.5 |
| Veri | Spring Data JPA (Hibernate), MySQL |
| Güvenlik | Spring Security (form tabanlı giriş, BCrypt şifreleme) |
| Görünüm | Thymeleaf, Thymeleaf Spring Security eklentisi |
| Ön yüz | Tailwind CSS (CDN), Chart.js, Font Awesome |
| Harita | Leaflet + OpenStreetMap katmanı |
| Diğer | Lombok, Bean Validation, Maven |

## Kurulum

### Gereksinimler

- JDK 21
- MySQL 8 veya üzeri
- (Maven kurulumu gerekmez, projedeki Maven Wrapper kullanılır)

### 1. Projeyi indirin

```bash
git clone https://github.com/sevvalunlu/voltflow.git
cd voltflow
```

### 2. Veritabanını oluşturun

MySQL'e bağlanıp boş bir veritabanı oluşturun. Tabloları uygulama ilk açılışta kendisi oluşturur.

```sql
CREATE DATABASE voltflowdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Ortam değişkenlerini ayarlayın

Gizli bilgiler koda yazılmaz, proje klasöründeki `.env` dosyasından okunur. Bu dosya `.gitignore` içindedir ve GitHub'a yüklenmez.

```bash
copy .env.example .env         # Windows
# cp .env.example .env         # macOS / Linux
```

`.env` dosyasını açıp kendi değerlerinizi yazın (değerleri tırnak içine almayın):

| Değişken | Açıklama |
| --- | --- |
| `DB_USERNAME` | MySQL kullanıcı adı |
| `DB_PASSWORD` | MySQL şifresi |
| `DB_URL` | (Opsiyonel) Varsayılan: `jdbc:mysql://localhost:3306/voltflowdb?...` |
| `VOLTFLOW_ADMIN_PASSWORD` | `admin` kullanıcısının şifresi |
| `VOLTFLOW_EMPLOYEE_PASSWORD` | `personel` kullanıcısının şifresi |

> Uygulama ilk açılışta `admin` (rol: `ADMIN`) ve `personel` (rol: `EMPLOYEE`) kullanıcılarını bu şifrelerle oluşturur. Şifre boş bırakılırsa ilgili kullanıcı oluşturulmaz. Şifreler veritabanına BCrypt ile şifrelenmiş olarak kaydedilir.

### 4. Uygulamayı başlatın

```bash
mvnw.cmd spring-boot:run       # Windows
# ./mvnw spring-boot:run       # macOS / Linux
```

Ardından tarayıcıda `http://localhost:8081` adresini açın.

## Proje Yapısı

```
voltflow/
├── src/main/java/com/voltflow/
│   ├── config/         # Spring Security ayarları ve ilk kullanıcı oluşturma
│   ├── controller/     # Sayfa ve uç nokta denetleyicileri
│   ├── entity/         # Scooter, MaintenanceLog, Notification, User, Location
│   ├── repository/     # Spring Data JPA depoları
│   ├── service/        # İş mantığı (ScooterService, NotificationService, ...)
│   └── exception/      # Global hata yönetimi
├── src/main/resources/
│   ├── templates/      # Thymeleaf sayfaları (dashboard, filo, harita, giriş, ...)
│   └── application.properties
├── .env.example        # Ortam değişkenleri şablonu
└── pom.xml
```

## Sayfalar

| Adres | Açıklama | Erişim |
| --- | --- | --- |
| `/` | Karşılama sayfası | Herkes |
| `/login` | Giriş | Herkes |
| `/dashboard` | Genel bakış ve grafikler | Giriş yapmış kullanıcı |
| `/scooters` | Araç filosu, filtreleme | Giriş yapmış kullanıcı |
| `/scooters/{id}` | Araç detayı ve bakım geçmişi | Giriş yapmış kullanıcı |
| `/scooters/new` | Yeni araç ekleme | Sadece `ADMIN` |
| `/scooters/delete/{id}` | Araç silme | Sadece `ADMIN` |
| `/map` | Canlı harita ve konum atama | Giriş yapmış kullanıcı |

## Geliştirici

**Şevval Ünlü**: [@sevvalunlu](https://github.com/sevvalunlu)
