# Otimizado v2: Baseado na v1 estável
FROM us-central1-docker.pkg.dev/intersec-56328/intersec-images/android-builder:v1

# Atualiza o SDK Manager e instala APENAS as versões 34 e 35 para garantir estabilidade
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" \
               "platforms;android-34" \
               "platforms;android-35" \
               "build-tools;34.0.0" \
               "build-tools;35.0.0"

# Garante permissões totais para o Gradle operar no SDK
RUN chmod -R 777 $ANDROID_HOME
