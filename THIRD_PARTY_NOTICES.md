# Third-Party Notices

This project bundles FFmpeg runtime binaries under:

`src/main/resources/assets/ffminepeg_api/bin/win-x64`

## FFmpeg

- Project: https://ffmpeg.org/
- Detected build flags from bundled binary include `--enable-gpl --enable-nonfree`.
- Running `ffmpeg.exe -L` reports:
  - `This version of ffmpeg has nonfree parts compiled in.`
  - `Therefore it is not legally redistributable.`

Important: even if this repository source code is GPL-2.0-or-later, the currently bundled FFmpeg binary build is marked by FFmpeg itself as non-redistributable. Replace FFmpeg with a redistributable build before public binary distribution if you need compliant releases.
