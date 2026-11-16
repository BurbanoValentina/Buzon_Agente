import { initializeApp } from "firebase/app";
import { getAnalytics, isSupported } from "firebase/analytics";

// Configuración de Firebase del proyecto buzonucc-5d67e
const firebaseConfig = {
  apiKey: "AIzaSyBiJjgmxp98oERdY33PZ06Ubkzuyuo_Cck",
  authDomain: "buzonucc-5d67e.firebaseapp.com",
  projectId: "buzonucc-5d67e",
  storageBucket: "buzonucc-5d67e.firebasestorage.app",
  messagingSenderId: "985405631900",
  appId: "1:985405631900:web:e94f931875407695c66c59",
  measurementId: "G-BE0D7KNTFL"
};

// Inicializar Firebase
export const app = initializeApp(firebaseConfig);

export let analytics = null;
if (typeof window !== "undefined") {
  isSupported().then((supported) => {
    if (supported) {
      analytics = getAnalytics(app);
    }
  });
}

export default app;
