# Google Play — Create these In-App Products

Match **exactly** (product IDs used in the Android binary — same as iOS):

| Product ID | Type | Purpose | Target price (USD) | Web/ZAR reference |
|---|---|---|---|---|
| `online.storytime.creators.upload.perfilm` | Managed product (consumable) | Per-film upload fee | **$9.99** | R99.99 |
| `online.storytime.creators.sub.upload.yearly` | Subscription (yearly) | Catalogue unlimited | **$59.99 / year** | R599.99 / year |
| `online.storytime.creators.sub.pipeline.monthly` | Subscription (monthly) | Full pipeline | **$19.99 / month** | R209.99 / month |
| `online.storytime.creators.sub.pipeline.yearly` | Subscription (yearly) | Full pipeline | **$199.99 / year** | R1,999.99 / year |

**Pay per film** plan itself is free to select in-app (no IAP). Upload fee is required on each “Submit for review”.

## Subscription group

Create one base plan group (e.g. “Creator Plans”) containing the three subscriptions above.

## Backend

Deploy `docs/server/android-purchase-route.ts` to production as:

`POST /api/creator/android/purchase`

(mirrors iOS `/api/creator/ios/purchase` with `provider: "GOOGLE_PLAY"`).

Until that route is live, license purchases fall back to `POST /api/creator/distribution-license` with the Google order id; upload-fee unlocks **require** the android purchase route.
