# SonarQube

Esta pasta guarda os artefactos da ficha 3 relativos ao SonarQube Cloud.

Cada membro do grupo deve guardar aqui o seu relatorio ou screenshot do SonarQube com
identificacao pelo respetivo numero de aluno, conforme pedido no enunciado.

Conteudos esperados por membro:
- screenshot ou relatorio PDF do SonarQube

Ficheiros esperados na `main`:
- `110894-eduardo-sonarqube.png`
- `111331-vasco-sonarqube.png`
- `123026-tiago-sonarqube.png`

Preparacao do repositorio:
- workflow em `.github/workflows/sonarcloud.yml`
- secret GitHub obrigatorio: `SONAR_TOKEN`
- sem uso de `SONAR_HOST_URL`

Validacao final pedida no enunciado:
- criar um erro temporario e controlado numa branch de teste ou PR
- confirmar a execucao do workflow SonarQube Cloud no GitHub Actions
- recolher o print ou relatorio individual no SonarQube Cloud
- remover o erro de teste depois da validacao
