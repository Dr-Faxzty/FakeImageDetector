# 🛡️ Fake Image Detector

Un'applicazione Android avanzata per l'identificazione di manipolazioni digitali e Deepfakes tramite un approccio ibrido: Analisi delle Frequenze (FFT) e Vision Transformers (ViT).

## 🚀 Caratteristiche
- **Analisi Ibrida**: Combina algoritmi matematici classici (Fast Fourier Transform) con il Deep Learning.
- **On-Device Inference**: Utilizza ONNX Runtime Mobile per eseguire l'IA localmente (privacy e velocità).
- **Security**: Sistema di autenticazione locale con gestione sicura delle credenziali.
- **Material Design**: Interfaccia moderna basata su Material 3.

## 🛠️ Stack Tecnologico
- **Sviluppo**: Java / Android SDK
- **IA**: ONNX Runtime, Hugging Face Transformers (per l'esportazione del modello).
- **Modello**: ViT-Tiny (Quantizzato a 8-bit).

## 📦 Installazione
1. Clona la repo: `git clone https://github.com/Dr-Faxzty/FakeImageDetector.git`
2. Apri con Android Studio Koala+.
3. Configura un emulatore con almeno 4GB di RAM e accelerazione hardware attiva.

## 🧠 Dettagli Tecnici (Esame)
L'applicazione è stata ottimizzata per dispositivi mobili tramite tecniche di **Model Quantization**, riducendo il peso del modello Vision Transformer del 75% senza compromettere significativamente l'accuratezza.