# Otimizado v2: Baseado na v1 estável
FROM us-central1-docker.pkg.dev/intersec-56328/intersec-images/android-builder:v1

# Atualiza o SDK Manager e instala as versões 36 e 37 para evitar downloads no build
# Incluímos as versões conhecidas para garantir a velocidade e estabilidade.
# Nota: Caso o canal estável do Google não possua a API 37 ainda, o comando pode falhar.
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" \
               "platforms;android-35" \
               "platforms;android-36" \
               "build-tools;35.0.0" \
               "build-tools;36.0.0" \
               "build-tools;36.1.0"

# Garante permissões totais para o Gradle operar no SDK
RUN chmod -R 777 $ANDROID_HOME
