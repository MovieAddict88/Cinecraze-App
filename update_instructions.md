# How to Add DRM Keys to ALL MPD Channels

## Step 1: Identify All MPD URLs
Your GitHub JSON contains these MPD channels that need DRM keys:

1. **HBO Family Asia** - `cg_hbofam.mpd`
2. **One Sport+** - `cg_onesportsplus_hd1.mpd` ✅ (you have this key)
3. **PBA Rush** - `cg_pbarush_hd1.mpd`
4. **NBA TV Philippines** - `nba_sdi.mpd`
5. **Premier Tennis HD** - `dr_premiertennishd.mpd`
6. **SPOTV HD** - `dr_spotvhd.mpd`
7. **Boomerang** - `boomerang/manifest.mpd`
8. **Cartoon Network HD** - `dr_cartoonnetworkhd.mpd`
9. **Animax** - `cg_animax_sd_new.mpd`
10. **Moonbug Kids** - `cg_moonbug_kids_sd.mpd`
11. **Asian Food Network** - `asianfoodnetwork_sd.mpd`
12. **Animal Planet** - `cg_animal_planet_sd.mpd`
13. **History HD** - `dr_historyhd.mpd`
14. **Discovery Asia** - `dr_discoveryasia.mpd`
15. **Knowledge Channel** - `kc/manifest.mpd`
16. **Travel Channel** - `travel_channel_sd.mpd`
17. **Hits Now** - `cg_hitsnow.mpd`
18. **Food Network HD** - `cg_foodnetwork_hd1.mpd`
19. **TLC** - `tlc_sd.mpd`
20. **AXN** - `cg_axn_sd.mpd`
21. **Channel News Asia** - `dr_channelnewsasia.mpd`
22. **BBC World News** - `bbcworld_news_sd.mpd`
23. **UAAP** - `cg_uaap_cplay_sd.mpd`
24. **GMA** - `abscbnono-dzsx9.amagi.tv/index.mpd`
25. **TV5** - `tv5_hd.mpd` ✅ (you have this key)
26. **Pinoy Box Office** - `pbo_sd.mpd`
27. **Viva Cinema** - `viva_sd.mpd`
28. **Cinema One** - `celmovie_pinoy_sd.mpd`
29. **Tagalog Movie** - `cg_tagalogmovie.mpd`
30. **Cinemax** - `cg_cinemax.mpd`
31. **KIX** - `kix/manifest.mpd`
32. **Thrill** - `cg_thrill_sd.mpd`
33. **HBO HD** - `cg_hbohd.mpd`
34. **Rock Extreme** - `dr_rockextreme.mpd`
35. **Hits Movies** - `dr_hitsmovies.mpd`
36. **DreamWorks** - `cg_dreamworktag.mpd`
37. **AZ2** - `tv5_hd.mpd` ✅ (you have this key)

## Step 2: Update Each Server Object

For each MPD server, add these fields:

```json
{
  "name": "HD",
  "url": "https://your-mpd-url.com/channel.mpd",
  "is_drm_protected": true,
  "drm_keys": "your_kid_here:your_key_here"
}
```

## Step 3: Keep Non-DRM Servers As-Is

For HLS (.m3u8) servers, just add:

```json
{
  "name": "720p",
  "url": "https://your-hls-url.com/stream.m3u8",
  "is_drm_protected": false
}
```

## Step 4: Example Complete Channel

```json
{
  "Title": "HBO Family Asia",
  "SubCategory": "Movies",
  "Country": "🌐 Asia",
  "Description": "HBO Family Asia is a pay television network that features family-friendly movies.",
  "Poster": "https://i.imgur.com/5krGyOX.png",
  "Thumbnail": "https://i.imgur.com/5krGyOX.png",
  "Rating": 0,
  "Duration": "",
  "Year": 0,
  "Servers": [
    {
      "name": "HD",
      "url": "https://qp-pldt-live-grp-03-prod.akamaized.net/out/u/cg_hbofam.mpd",
      "is_drm_protected": true,
      "drm_keys": "872910C8-4329-4319-800D-85F9A0940607:your_actual_hbo_key_here"
    }
  ]
}
```

## Your Known Keys:
- **AZ2/TV5**: `f703e4c8ec9041eeb5028ab4248fa094:c22f2162e176eee6273a5d0b68d19530`
- **TV5**: `2615129ef2c846a9bbd43a641c7303ef:07c7f996b1734ea288641a68e1cfdc4d`
- **One Sports+**: `322d06e9326f4753a7ec0908030c13d8:1e3e0ca32d421fbfec86feced0efefda`

## What You Need:
- Get the remaining ~34 key/ID pairs for the other MPD channels
- Add them to your GitHub JSON using the format above
- No Java code changes needed!