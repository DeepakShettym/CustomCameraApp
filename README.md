# CustomCameraApp
An Android app that opens the camera, previews, and captures images with storage support

📷 Custom Camera App for ARM Android (MYC-LT527)

An Android camera application developed and tested on the MYC-LT527 System-on-Module (SoM), designed to demonstrate how to open, preview, and capture images on embedded ARM-based Android systems using the legacy Camera API.

🧠 Overview

This project provides a simple yet effective interface to:

Initialize and control camera hardware on ARM Android boards

Start/stop live preview using a SurfaceView

Capture images directly from the device camera

Store captured images into the Android MediaStore

The app was specifically developed for MYIR’s MYC-LT527 module, ensuring compatibility with embedded Android environments and custom camera HALs.

📱 Application Features
Function	Description
🎥 Camera Control	Open and close camera via button controls
🖼️ Preview	Start and stop live camera feed using SurfaceView
📸 Capture	Capture images and confirm functionality
💾 Storage	Save captured images in /Pictures/CustomCameraApp/
🔒 Permission Handling	Runtime CAMERA and STORAGE permission support
🧩 Reflection-Based Open	Uses custom Camera.open() method with parameters for custom HAL compatibility

🧠 Architecture Flow

+------------------------------------------------+

|                  Android App                   |

|  - SurfaceView (Preview)                       |

|  - Buttons: Open, Start, Stop, Capture, Close  |

+---------------------+--------------------------+

                      |
                      
                      v

+------------------------------------------------+

|             Android Framework Layer            |

|  - Camera API (Legacy Java API)                |

|  - MediaStore for saving images                |

+---------------------+--------------------------+

                      |
                      
                      v

+------------------------------------------------+

|          HAL / Kernel Driver (C++)             |

|  - Custom Camera HAL on ARM-based SoM          |

|  - Communicates with sensor via MIPI-CSI       |

+------------------------------------------------+


🛠️ Implementation Steps

SurfaceView Setup
Created a preview surface in the layout for live camera feed.

Button Controls
Added buttons to:

Open camera

Start preview

Stop preview

Capture image

Close camera

Custom Camera Open (Reflection)
Used:

Method open = cls.getMethod("open", int.class, int.class, int.class, int.class, int.class);
camera = (Camera) open.invoke(null, 16, 1, 2592, 1944, 0);


Enables opening camera with board-specific parameters.

Image Capture
Implemented Camera.PictureCallback to receive image data and store it via Android’s MediaStore.

Permission Handling
Handled both CAMERA and WRITE_EXTERNAL_STORAGE permissions dynamically at runtime.

Testing on MYC-LT527
Verified open, preview, capture, and image saving on custom Android build running on MYC-LT527 SoM.

💾 Image Storage Path

Captured images are saved under:

/storage/emulated/0/Pictures/CustomCameraApp/

