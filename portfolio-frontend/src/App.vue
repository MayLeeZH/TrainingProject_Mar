<<template>
  <div v-if="!authStore.isAuthenticated">
    <Login />
  </div>

  <div v-else>
    <router-view></router-view>
  </div>
</template>

<script setup>
import { useAuthStore } from './stores/authStore';
import Login from './pages/Login.vue';
// no need import,router will handle it

const authStore = useAuthStore();
</script>

<style>
/* We delete the old layout styles because Dashboard.vue handles all of that now.
  We just need to strip out the browser's default margins so the sidebar 
  touches the very edge of the screen.
*/
body {
  margin: 0;
  padding: 0;
  background-color: #0a0a0c; /* Matches your dashboard background */
}
</style>