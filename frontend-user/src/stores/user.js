import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { authApi, userApi } from "../api";

export const useUserStore = defineStore("user", () => {
  const token = ref(localStorage.getItem("token") || "");
  const user = ref(JSON.parse(localStorage.getItem("user") || "null"));
  const settings = ref(
    JSON.parse(
      localStorage.getItem("userSettings") || '{"soundEnabled": true}',
    ),
  );

  const isLoggedIn = computed(() => !!token.value);
  const soundEnabled = computed(() => settings.value.soundEnabled ?? true);

  async function login(username, password) {
    const res = await authApi.login({ username, password });
    token.value = res.data.token;
    user.value = res.data.user;
    localStorage.setItem("token", res.data.token);
    localStorage.setItem("user", JSON.stringify(res.data.user));
    if (res.data.user.settings) {
      try {
        settings.value = JSON.parse(res.data.user.settings);
        localStorage.setItem("userSettings", JSON.stringify(settings.value));
      } catch (e) {
        console.error("Failed to parse user settings", e);
      }
    }
    return res;
  }

  async function register(username, password, nickname) {
    const res = await authApi.register({ username, password, nickname });
    token.value = res.data.token;
    user.value = res.data.user;
    localStorage.setItem("token", res.data.token);
    localStorage.setItem("user", JSON.stringify(res.data.user));
    return res;
  }

  async function fetchUserInfo() {
    const res = await authApi.getInfo();
    user.value = res.data;
    localStorage.setItem("user", JSON.stringify(res.data));
    if (res.data.settings) {
      try {
        settings.value = JSON.parse(res.data.settings);
        localStorage.setItem("userSettings", JSON.stringify(settings.value));
      } catch (e) {
        console.error("Failed to parse user settings", e);
      }
    }
    return res;
  }

  async function updateSettings(newSettings) {
    settings.value = { ...settings.value, ...newSettings };
    localStorage.setItem("userSettings", JSON.stringify(settings.value));
    if (isLoggedIn.value) {
      try {
        await userApi.updateSettings(JSON.stringify(settings.value));
      } catch (e) {
        console.error("Failed to update settings on server", e);
      }
    }
  }

  function logout() {
    token.value = "";
    user.value = null;
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  }

  return {
    token,
    user,
    settings,
    isLoggedIn,
    soundEnabled,
    login,
    register,
    fetchUserInfo,
    updateSettings,
    logout,
  };
});
