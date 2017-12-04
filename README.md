# Screen Stealer

Take screenshots and control mouse location (Don't use that on devices that are not yours)

Takes screenshots of the victim screens and tells you what they're doing.

The application is based on client server architecture, the client is the victim. It connects to the server and sends screenshots to it.

The server  can control the mouse location of the victim device.

## How to run
1) Start the ScreenSpyServer application
2) Run the client after adjusting the server's ip (Default is localhost)

### Might happen in the future

This application can be easily extended to a remote desktop application. 

That requires implementing mouse clicks which is very easy, hand handling hover events over the GUI window to sync the mouse motion in the 2 Devices