<template>
  <div id="app" class="container my-3">
    <h2 class="text-ccenter">Revision</h2>
    <h4 class="text-center">Validador automático de trabalhos dos estudantes de cursos de</h4>

    <div class="card mt-3">
      <div class="card-body">

        <div class="form-group">
          <label for="githubProfileURL">Insira o link do seu perfil no Github</label>
          <input type="text" class="form-control" ref="githubProfileURL" placeholder="Exemplo: https://github.com/psantunes" />
        </div>

        <div class="form-group">
          <label for="moodleProfileURL">Insira o link do seu perfil no Moodle</label>
          <input type="text" class="form-control" ref="moodleProfileURL" placeholder="Exemplo: http://localhost/user/profile.php?id=5" />
        </div>

        <div class="form-group">
          <label for="moodleAssignURL">Insira o link da tarefa no Moodle</label>
          <input type="text" class="form-control" ref="moodleAssignURL" placeholder="Exemplo: http://localhost/mod/assign/view.php?id=2" />
        </div>

        <button class="btn btn-primary" @click="postData">Enviar dados</button>
        <button class="btn btn-secondary ml-2" @click="clearForm">Limpar</button>


        <div v-if="postResult" class="alert alert-success mt-3" :class="{ 'alert-danger': isError }" role="alert">{{postResult}} </div>
      </div>
    </div>
 
  </div>
</template>

<script>
const baseURL = "http://localhost:8080";

export default {
  name: "App",
  data() {
    return {
      postResult: null,
      isError: true,
    }
  },
  methods: {
    fortmatResponse(res) {
    const obj = JSON.parse(JSON.stringify(res));
    return obj.Message;
    },

    async postData() {

      const params = new URLSearchParams();
      params.append("githubProfileURL", this.$refs.githubProfileURL.value);
      params.append("moodleProfileURL", this.$refs.moodleProfileURL.value);
      params.append("moodleAssignURL", this.$refs.moodleAssignURL.value);

      try {
        const res = await fetch(`${baseURL}/check`, {
          method: "post",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: params});

        const data = await res.json();

        this.isError = res.ok ?  false : true;

        this.postResult = this.fortmatResponse(data);

      } catch (err) {
        this.postResult = err.message;
      }
    },

    clearForm() {
      this.postResult = null;
    },

  }
}
</script>

<style>
#app {
  max-width: 600px;
  margin: auto;
}
</style>
