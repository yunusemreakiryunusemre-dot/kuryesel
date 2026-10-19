# KuryeCüzdan - Çift Kasa & Nakit Tahsilat Mahsuplaşma Mimarisi

Motosikletli kuryelerin sahada karşılaştığı en büyük finansal karmaşa olan **"Kapıda Nakit Tahsilat"** ile **"Hak Ediş"** arasındaki mahsuplaşma mantığını çözen, iki ayrı kasa (Cepteki Nakit & Hak Ediş Alacağı) ve hızlı bahşiş entegrasyonu planı.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> Kullanıcı revizyon talebi doğrultusunda güncellenen kurallar:

- **Confirmed Decision 1 (Hak Ediş Terminolojisi)**: "Banka Hesabı" ifadesi tamamen kaldırılarak yerine kuryelerin kullandığı **"Hak Ediş"** terminolojisi entegre edilmiştir.
  - Kasa 1: **"Cepteki Nakit Kasa"** (Eldeki nakit).
  - Kasa 2: **"Şirket Hak Ediş Alacağı"** (Firmadan içeride biriken bekleyen hak ediş).
  - Ödeme Onayı: Ayarlar ekranındaki *"Hak Ediş Yönetimi"* menüsünden *"Hak Ediş Hesaba Yattı (Ödemeyi Onayla)"* butonuna basıldığında biriken alacak **"Ödenen Hak Ediş"** olarak arşivlenecek ve bekleyen alacak sıfırlanacaktır.
- **Confirmed Decision 2 (Varsayılan Harcama Kasası)**: Benzin, bakım vb. yeni eklenen tüm giderler varsayılan olarak kuryenin elindeki **Cepteki Nakit Kasa**'sından düşülecektir.
- **Confirmed Decision 3 (Hızlı Bahşiş Butonları)**: Ana sayfada tek tıkla elden alınan bahşişleri işleyen **+₺20, +₺50, +₺100** ve özel tutar girişi çipleri yer alacaktır.

---

### 1. Overview & Core Concept

1. **Cepteki Nakit Kasa (Fiziksel Para)**:
   - Kuryenin cebinde fiziki olarak taşıdığı paradır.
   - Kapıda müşteriden nakit sipariş bedeli alındığında **artar**.
   - Elden bahşiş alındığında **artar**.
   - Günlük benzin, yemek veya nakit harcama yapıldığında **azalır**.

2. **Şirket Hak Ediş Alacağı (Trendyol, Getir, Yemeksepeti, Vigo vb.)**:
   - Paket başı kazançlar ve bonuslar eklendiğinde **artar**.
   - Müşteriden **kapıda nakit tahsilat** yapıldığında (örneğin 320 ₺ nakit alındığında), bu para kuryenin cebinde kaldığı için firma kuryenin hak edişinden 320 ₺ keser (**otomatik mahsuplaşma -320 ₺**).
   - Hak ediş hesaba yatıp onaylandığında **"Ödenen Hak Ediş"** statüsüne geçer.

3. **Net Cüzdan Varlığı**:
   - `Cepteki Nakit + Bekleyen Hak Ediş` toplamı kuryenin anlık net durumunu gösterir.

---

### 2. User Experience & Visual Design

#### Ana Sayfa Yenilikleri
- **İkili Kasa Kartları**:
  - Sol/Üst Kart: **"Cepteki Nakit Kasa"** (💵 Yeşil neon vurgulu, eldeki fiziksel nakit para).
  - Sağ/Alt Kart: **"Şirket Hak Ediş Alacağı"** (🏢 Canlı turuncu vurgulu, firmada bekleyen net hak ediş).
- **Hızlı Bahşiş Çipleri**:
  - Ana 3 butonun hemen altında: `[ +₺20 ]`, `[ +₺50 ]`, `[ +₺100 ]`, `[ Diğer ]`.
  - Sipariş teslim edilip bahşiş alındığında tek tıkla cebe nakit eklenir.
- **İşlem Ekle (+) Diyaloğu**:
  - 3 Seçenekli İşlem Tipi:
    1. **Hak Ediş Ekle (+)** -> Şirket Hak Ediş Alacağını artırır.
    2. **Kapıda Nakit Tahsilat (💵)** -> Cebe nakit ekler (+), Hak Ediş Alacağından otomatik düşer (-).
    3. **Gider / Masraf (-)** -> Zarf seçimi, KDV ayrıştırması, nakit kasadan düşme.

#### Ayarlar / Menü Ekranı Yenilikleri
- **"Hak Ediş Yönetimi" Bölümü**:
  - İçeride bekleyen hak ediş tutarı özeti.
  - Yanlış tıklamayı önleyen onay pencereli **"Hak Ediş Hesaba Yattı (Ödemeyi Onayla)"** butonu.

---

### 3. Data Model & Architecture Update

#### TransactionEntity
```kotlin
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val transactionType: String, // "HAKEDIS", "CASH_COLLECTION", "TIP", "EXPENSE", "HAKEDIS_PAYOUT"
    val isIncome: Boolean,
    val envelopeId: Int? = null,
    val envelopeName: String? = null,
    val vatRate: Int = 20,
    val vatAmount: Double = 0.0,
    val baseAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)
```

#### CourierSummary & ViewModel Hesaplama Mantığı
- `pocketCash`:
  `sum(CASH_COLLECTION) + sum(TIP) - sum(EXPENSE)`
- `companyReceivable` (Bekleyen Hak Ediş):
  `sum(HAKEDIS) - sum(CASH_COLLECTION) - sum(HAKEDIS_PAYOUT)`
- `paidHakedis` (Ödenen Hak Ediş):
  `sum(HAKEDIS_PAYOUT)`
- `netBalance`:
  `pocketCash + companyReceivable`

---

### Verification Plan

1. **Mahsuplaşma Testi**: 1.000 ₺ Hak Ediş eklendiğinde Hak Ediş = 1.000 ₺, Nakit = 0 ₺. Ardından 300 ₺ Kapıda Nakit Tahsilat yapıldığında; Cepteki Nakit = +300 ₺, Şirket Hak Ediş Alacağı = 700 ₺, Toplam Net = 1.000 ₺ olmalı.
2. **Hızlı Bahşiş Testi**: +50 ₺ bahşişe basıldığında Cepteki Nakit 350 ₺ olmalı, Hak Ediş Alacağı değişmemeli.
3. **Gider Testi**: 100 ₺ Benzin harcandığında Cepteki Nakit 250 ₺'ye inmeli, %20 KDV indirimi hesaplanmalı.
4. **Hak Ediş Onay Testi**: Ayarlar ekranından "Hak Ediş Hesaba Yattı" onaylandığında 700 ₺ Hak Ediş Alacağı sıfırlanıp "Ödenen Hak Ediş" hanesine yazılmalı.
5. **Derleme Doğrulaması**: `compile_applet` ile projenin hatasız derlenmesi.
