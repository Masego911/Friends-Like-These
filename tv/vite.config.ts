import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"
import legacy from "@vitejs/plugin-legacy"

export default defineConfig({
  plugins: [
    react(),
    legacy({
      targets: [
        "chrome >= 38",
        "safari >= 9",
        "edge >= 15",
        "firefox >= 45"
      ]
    })
  ],

  server: {
    proxy: {
      "/api": {
        target: "https://friends-like-these-api-e4ftgjdgftaqc7cr.southafricanorth-01.azurewebsites.net",
        changeOrigin: true,
        secure: true
      }
    }
  },

  preview: {
    proxy: {
      "/api": {
        target: "https://friends-like-these-api-e4ftgjdgftaqc7cr.southafricanorth-01.azurewebsites.net",
        changeOrigin: true,
        secure: true
      }
    }
  }
})