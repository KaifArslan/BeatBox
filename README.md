# 🎵 BeatBox - Java Music Collaboration Tool

## 🚀 Introduction
BeatBox is a Java-based music sequencer that allows users to create beats, play MIDI sounds, and share their beats over a network. This project uses **Swing** for UI, **MIDI** for sound generation, **Serialization** for saving/loading, and **Sockets** for network communication.

---

## 📌 How to Run the Project

### **1️⃣ Start the Music Server**
Before launching the BeatBox client, start the server to handle network connections.

```bash
javac Music_Server.java
java Music_Server
```

### **2️⃣ Run the BeatBox Client**
Once the server is running, launch the BeatBox application.

```bash
javac BeatBox.java
java BeatBox <YourName>
```

### **📝 Note:**
- You can directly **pass your name** as a command-line argument (`java BeatBox YourName`).
- If you don't provide a name, a **dialog box** will prompt you to enter your name.
- Currently, the application is **set to localhost** for testing. If running on a network, update the IP address in the code.

---

## 🔧 Features
✅ **Create & play beats** using MIDI sounds 🎶  
✅ **Save & load beats** using Serialization 💾  
✅ **Network sharing** to collaborate with others 🌍  
✅ **Swing-based GUI** for an interactive experience 🎨  

---

## 💡 Future Improvements
- ✅ Implement **custom server configuration** instead of localhost.
- ✅ Improve **UI/UX** with modern JavaFX.
- ✅ Add **multi-user chat integration**.

---

## 📜 License
This project is open-source and available under the **MIT License**.

---

### ✨ Made with ❤️ by Mohammad Kaif
