# Alic AI — Native Android AI Voice Assistant

**Alic AI** is an advanced native Android AI voice assistant app developed by **Rohit Raj**, powered by **Google Gemini API**, native Android **SpeechRecognizer**, high-quality neural **TextToSpeech** for non-robotic natural voice, **AccessibilityService** for automatic app & YouTube controls, and hands-free continuous conversation.

---

## 🚀 Direct APK Download (बिना ZIP और बिना टर्मिनल के डायरेक्ट डाउनलोड)

अगर आप बिना किसी Terminal और बिना ZIP फाइल निकाले सीधे अपने Android फोन में APK डाउनलोड करना चाहते हैं:

### तरीका (Direct 1-Click APK):
1. अपने GitHub रिपॉजिटरी पेज पर जाएं।
2. दाईं ओर (Right sidebar) या मेनू में **"Releases"** पर क्लिक करें।
3. वहाँ आपको **"Alic AI v1.0 Direct APK"** दिखेगा।
4. **Assets** के अंदर **`Alic-AI.apk`** पर क्लिक करें।
5. आपका मोबाइल ब्राउज़र (Google Chrome) सीधे **`Alic-AI.apk`** डाउनलोड कर लेगा — **कोई ZIP नहीं, कोई एक्सट्रैक्शन नहीं, कोई टर्मिनल नहीं!**
6. डाउनलोड होने के बाद नोटिफिकेशन पर टैप करके सीधे इंस्टॉल करें।

---

## ✨ मुख्य फीचर्स (Key Features)

### 1. जिमिनी का नेचुरल पर्सोना (Gemini Non-Robotic Voice Persona)
- **Ultra Natural & Human-like Voice**: रोबोटिक आवाज़ को पूरी तरह हटाकर नेचुरल न्यूरल वॉइस इंजन का उपयोग।
- **Conversational Hindi & English**: हिंदी, हिंग्लिश और अंग्रेजी में सहज और दोस्ताना अंदाज़ में बात करता है।
- **Gemini Live Style Speech**: जवाबों को छोटा, सटीक और आवाज़ में सुनने लायक बनाया गया है।

### 2. निरंतर बातचीत मोड (Continuous Hands-Free Conversation)
- **बार-बार माइक दबाने की जरूरत नहीं**:
  - बस 1 बार माइक बटन दबाएं।
  - आप जो बोलेंगे, Alic AI सुनेगा और Gemini से जवाब देगा।
  - जैसे ही Alic बोलना समाप्त करेगा, **यह तुरंत अपने आप दोबारा आपकी बात सुनने लगेगा!**
  - जब तक आप खुद बात बंद न करना चाहें या Stop बटन न दबाएं, बातचीत लगातार चलती रहेगी।

### 3. एक्सेसिबिलिटी परमिशन और YouTube ऑटो-प्ले (Accessibility & YouTube Auto-Play)
- **"YouTube ओपन करके यो यो हनी सिंह का पहला गाना प्ले करो"**:
  - जैसे ही आप यह कमांड बोलते हैं, Alic AI YouTube खोलता है।
  - अगर एक्सेसिबिलिटी परमिशन चालू है, तो यह स्क्रीन पर पहले वीडियो को अपने आप डिटेक्ट करके **Play** कर देता है!
- **एक्सेसिबिलिटी परमिशन कैसे चालू करें**:
  - होम स्क्रीन पर दिए गए **"एक्सेसिबिलिटी चालू करें"** कार्ड पर टैप करें।
  - यह सीधे आपके फोन की Accessibility Settings खोल देगा।
  - वहाँ **Alic AI Agent Control** को चालू (Turn ON) कर दें।
  - इसके बाद Alic AI आपके फोन में कोई भी ऐप खोल सकता है और क्लिक कर सकता है।

### 4. ऐप और हार्डवेयर कंट्रोल्स (Phone & Hardware Controls)
- **टॉर्च**: *"टॉर्च चालू करो"*, *"टॉर्च बंद करो"* (Flashlight on/off).
- **ऐप्स**: *"WhatsApp खोलो"*, *"Camera चालू करो"*, *"Instagram खोलो"*.
- **नेविगेशन**: *"Google Maps खोलो"*, *"रास्ता दिखाओ"*.
- **आवाज़**: *"वॉल्यूम बढ़ाओ"*, *"आवाज़ कम करो"*.
- **अलार्म**: *"अलार्म सेट करो"*.
- **स्क्रीन विज़न**: *"स्क्रीन पर क्या है?"* (Gemini Screen Vision).

### 5. सुरक्षित Keystore API Key मैनेजमेंट
- ऐप के अंदर Settings में जाकर अपनी खुद की **Gemini API Key** डालें।
- Android Keystore AES-GCM एन्क्रिप्शन के साथ पूरी तरह सुरक्षित।
- **Test Connection** बटन से तुरंत वेरीफाई करें।

---

## 🛠️ GitHub Actions Workflow (Automatic Build)

`.github/workflows/build-apk.yml` फाइल रिपॉजिटरी में कॉन्फ़िगर की गई है:
- जब भी आप `main` ब्रांच में कोड पुश करते हैं, यह स्वचालित रूप से रन होती है।
- **Debug APK** कंपाइल करती है।
- **GitHub Release** पर सीधे **`Alic-AI.apk`** पब्लिश करती है, जिससे कोई भी बिना ZIP के सिंगल क्लिक में डाउनलोड कर सकता है।

---

## 👨‍💻 Developer & Credits

- **Developer**: Rohit Raj
- **Project**: Alic AI Android Voice Assistant
- **Engine**: Google Gemini API + Android Jetpack Compose + Accessibility Automation
- **License**: MIT
