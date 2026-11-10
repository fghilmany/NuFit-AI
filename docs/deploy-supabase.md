# Deploy AI Backend (Supabase Edge Functions) — Panduan

Panduan ini untuk deploy backend AI (`ai-custom-plan-generate`, `chat-send`, `meal-plan-generate`)
ke Supabase. Ditulis untuk yang belum familiar backend — tiap langkah dijelaskan kenapa perlu.
Lihat `docs/decisions.md` ADR-008 untuk keputusan arsitekturnya.

## 0. Prasyarat

- Akun [Supabase](https://supabase.com) + project sudah dibuat (project ref: `wnruvcpeyxssxpqwptst`).
- Akun Surplus Intelligence (provider LLM yang dipakai, `api.surplusintelligence.ai`) — buat API key di dashboard akun kamu di sana.
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) terinstall & nyala (dipakai kalau mau testing lokal, langkah 7).

## 1. Install Supabase CLI

```
npm install -g supabase
```

## 2. Kumpulkan key yang dibutuhkan

Semua ada di dashboard Supabase → **Settings** → **API**:

| Key | Kegunaan | Catatan |
|---|---|---|
| **anon public** | Dipakai client (app) & buat login test user | Boleh dilihat publik |
| **service_role** | Admin — bypass semua permission | **RAHASIA**, jangan pernah taruh di kode app/client |
| **Project Reference** (bagian "Reference used in APIs and URLs") | Identitas project | Untuk `wnruvcpeyxssxpqwptst`, ini sudah dipakai di semua URL contoh dokumen ini |

## 3. Login CLI

`supabase login` butuh browser interaktif — kalau dijalankan lewat command non-interaktif (mis. dari sesi Claude Code), akan gagal. Pakai token manual:

1. Buka https://supabase.com/dashboard/account/tokens → **Generate new token**.
2. Jalankan:
```
supabase login --token <token-tadi>
```

## 4. Hubungkan folder project ke project Supabase

**Wajib dijalankan dari dalam folder `App/NuFitAi-Claude`** (folder yang punya subfolder `supabase/`):
```
cd /Users/faris/Documents/learn/gym-app/App/NuFitAi-Claude
supabase link --project-ref wnruvcpeyxssxpqwptst
```
Kalau diminta password database, itu password Postgres project (Settings → Database → reset kalau lupa).

## 5. Upload tabel (migration)

Membuat tabel `user_entitlement`, `ai_usage`, `exercise` (+ seed data ~1300 exercise):
```
supabase db push
```

## 6. Set API key AI (secret, untuk versi cloud/deployed)

```
supabase secrets set LLM_API_KEY=<key-surplus-intelligence-kamu>
```
Catatan: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY` **tidak perlu** di-set manual — otomatis tersedia untuk function yang sudah deploy.

## 7. Deploy function

```
supabase functions deploy ai-custom-plan-generate
supabase functions deploy chat-send
supabase functions deploy meal-plan-generate
```
Setelah sukses, alamat yang bisa dipanggil:
```
https://wnruvcpeyxssxpqwptst.supabase.co/functions/v1/<nama-function>
```
Update `BASE_URL` di 3 file ini kalau project ref berubah (project ref sekarang sudah benar di kode):
- `shared/.../data/aicustomplan/datasource/AiCustomPlanRemoteDataSource.kt`
- `shared/.../data/chatai/datasource/ChatRemoteDataSource.kt`
- `shared/.../data/mealplan/datasource/MealPlanRemoteDataSource.kt`

## 7b. (Opsional) Testing lokal, tanpa deploy berkali-kali

Untuk iterasi cepat tanpa `deploy` tiap ubah kode:

1. Buat file `supabase/.env.local` (sudah di-`.gitignore`, aman):
```
SUPABASE_URL=https://wnruvcpeyxssxpqwptst.supabase.co
SUPABASE_ANON_KEY=<anon-key>
SUPABASE_SERVICE_ROLE_KEY=<service-role-key>
LLM_API_KEY=<key-surplus-intelligence-kamu>
```
Ini bikin function yang jalan di laptop tetap connect ke database Supabase asli (cloud), cuma proses function-nya yang lokal.

2. Jalankan:
```
supabase functions serve --env-file ./supabase/.env.local
```
(Docker Desktop harus nyala. Biarkan terminal ini tetap jalan.)

3. Function bisa dipanggil di `http://localhost:54321/functions/v1/<nama-function>`.

4. Tiap ubah kode `.ts`: `Ctrl+C` di terminal itu, jalankan ulang command di step 2.

**Catatan:** login/auth (dapat `access_token`) harus tetap lewat URL **cloud** (`https://wnruvcpeyxssxpqwptst.supabase.co/auth/v1/token`), bukan localhost — karena user asli cuma ada di database cloud, bukan di stack lokal.

## 8. Setup user testing (sekali per user)

Endpoint-endpoint ini butuh user yang sudah login + (untuk AI Custom Plan/Meal Plan) tier `SUBSCRIBED`.

**Kalau user login via Google** (tidak punya password) — set password dulu supaya bisa dites lewat Postman:
```
PUT https://wnruvcpeyxssxpqwptst.supabase.co/auth/v1/admin/users/<user-id>
Header: apikey: <service_role-key>, Authorization: Bearer <service_role-key>
Body: { "password": "Test1234!" }
```

**Login, dapat access_token:**
```
POST https://wnruvcpeyxssxpqwptst.supabase.co/auth/v1/token?grant_type=password
Header: apikey: <anon-key>
Body: { "email": "<email-user>", "password": "Test1234!" }
```
→ ambil `access_token` dari response, dipakai di header `Authorization: Bearer <access_token>` tiap panggil endpoint AI.

**Set tier jadi Subscribed** (SQL Editor di dashboard):
```sql
insert into user_entitlement (user_id, tier) values ('<user-id>', 'SUBSCRIBED')
on conflict (user_id) do update set tier = 'SUBSCRIBED';
```

**Lihat semua user + tier-nya:**
```sql
select u.id, u.email, coalesce(e.tier, 'LOGGED_IN (default)') as tier
from auth.users u left join user_entitlement e on e.user_id = u.id
order by u.created_at desc;
```

## Troubleshooting

| Gejala | Penyebab | Solusi |
|---|---|---|
| `No API key found in request` | Header `apikey` tidak kekirim (disable/typo/spasi) | Cek checkbox header aktif di Postman, ketik ulang manual |
| `Invalid login credentials` (400) saat login | Step set-password sebenarnya gagal duluan | Cek response step set-password itu benar status 200 |
| 401 tanpa error message di response/log | Ini **normal** — kode sengaja tidak nge-log kegagalan auth. Biasanya `access_token` sudah expired (~1 jam) | Login ulang, dapat token baru. Cek juga dengan `GET /auth/v1/user` pakai token itu — kalau ikut gagal, tokennya memang tidak valid |
| Error 500, response cuma `{"error":"internal"}` | Sengaja generik (keamanan) | Cek detail asli di dashboard → Edge Functions → nama function → Logs |
| `429 RESOURCE_EXHAUSTED` (Gemini) | Provider lama; sudah tidak dipakai (pindah ke Surplus Intelligence) | — |
| `401 invalid_api_key` dari Surplus Intelligence | Secret `LLM_API_KEY` belum di-set / salah / expired | `supabase secrets set LLM_API_KEY=<key-valid>`, gak perlu redeploy function |
| Kode sudah diedit tapi error masih sama persis | `supabase functions serve` di terminal lama belum di-restart | `Ctrl+C`, jalankan ulang step 7b.2 |
