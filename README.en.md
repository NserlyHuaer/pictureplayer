# PicturePlayer

A cross-platform image browser for viewing and managing images, supporting advanced image processing features and automatic version updates.

## Features

- Supports multiple image formats (JPEG, TIFF, PNG, WebP, etc.)
- Provides image zooming, rotation, and full-screen viewing capabilities
- Multi-threaded image blur processing
- Automatically detects and downloads new versions
- Supports proxy server settings
- Cross-platform support (Windows/Linux/macOS)

## Installation Instructions

1. Download the latest version of `PicturePlayer.jar`
2. Ensure that a Java Runtime Environment (JRE 8 or later) is installed
3. Run the jar file by double-clicking it, or use the command line:
```bash
java -jar PicturePlayer.jar
```

## Usage Instructions

1. Open images using the file selector
2. Use toolbar buttons to perform rotation, zooming, and reset operations
3. Supports drag-and-drop for viewing large images
4. In the settings panel, you can adjust:
   - Hardware acceleration
   - Proxy server
   - Update checking
   - Interface display options

## Developer Tools

This project includes the following utility classes:
- Image information retrieval
- File download management
- System resource monitoring
- Multilingual support
- Custom components (transparent panel, percentage label, etc.)

## Version Updates

The application automatically checks for updates on startup, or you can manually click the "Check for Updates" button. The update process includes:
1. Detecting the operating system type
2. Downloading update files
3. Visual progress display
4. Automatic restart for update installation

## Dependencies

- ImageIO extension library (supports additional image formats)
- Log4j2 (logging management)
- Gson (JSON processing)
- Thumbnailator (thumbnail generation)
- JNA (native call support)

## License

This project uses the [MIT License](LICENSE). Please refer to the LICENSE file for details.

## Project Structure

```
src/
├── main/
│   ├── java/           # Java source code
│   ├── resources/      # Resource files (configuration, internationalization, etc.)
│   └── Generate/       # Version information generation tool
└── artifacts/          # Build output directory
```