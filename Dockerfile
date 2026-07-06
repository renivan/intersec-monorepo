# Otimizado v2: Baseado na v1 estável
FROM us-central1-docker.pkg.dev/intersec-56328/intersec-images/android-builder:v1

# Atualiza o repositório e instala os SDKs 35, 36 e 37
# Usamos o comando update para garantir que as versões de 2026 estejam visíveis
RUN sdkmanager --update && \
    yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" \
               "platforms;android-35" \
               "platforms;android-36" \
               "platforms;android-37" \
               "build-tools;35.0.0" \
               "build-tools;36.0.0" \
               "build-tools;37.0.0"

# Garante permissões totais para o Gradle operar no SDK
RUN chmod -R 777 $ANDROID_HOME
