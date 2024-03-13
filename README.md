# CodeXvert - Manage and Transcode Your Video Library

CodeXvert is a modern web application built with Kotlin Multiplatform that simplifies managing and transcoding your video library. It provides a user-friendly interface to browse your videos, filter them by codecs, and convert between different video and audio codecs with ease.

## Features

- **Video Library Management**: Organize and search your video files with support for various file extensions like AVI, MP4, MKV, and MPEG4.
- **Codec Filtering**: Filter your videos based on video and audio codecs for better organization and easy identification.
- **Transcoding**: Convert between different video and audio codecs with a simple interface. Supported codecs include H.264, VP9, AAC, MP3, Opus, and Vorbis (among others).
- **Multi-Platform Support**: CodeXvert is built with Kotlin Multiplatform, allowing it to run on the Web, Desktop, and Server environments.

## Getting Started

To get started with CodeXvert, you can either run the application locally or deploy the Docker container.

### Running Locally

1. Clone the repository: `git clone https://github.com/jsixface/codexvert.git`
2. Navigate to the project directory: `cd codexvert`
3. Build the project: `./gradlew build`
4. Run the web application: `./gradlew :server:run`
5. Access the application in your browser at `http://localhost:8080`

### Docker Deployment

CodeXvert is available as a Docker image on the GitHub Container Registry. You can pull and run the latest image with the following command:

```bash
docker run -d -p 8080:8080 ghcr.io/jsixface/codexvert:latest
```
This will start the CodeXvert application and expose it on http://localhost:8080.

## License

CodeXvert is released under the AGPL-3.0 License.

