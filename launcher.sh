#!/bin/bash

# Inicia o servidor do miniTruco e monitora o JAR dele (que é deletado
# e recriado pelo processo de deploy).
#
# Quando o JAR é deletado, o script envia um sinal SIGUSR1 para o servidor,
# o que faz o servidor entrar em "shutdown suave" (libera a porta imediatamente,
# mas finaliza as partidas em andamento antes de se auto-encerrar)
#
# Quando o JAR é recriado, o script inicia outra instância do servidor.
#
# Desta forma, o servidor pode ser atualizado sem interromper as partidas e
# com downtime mínimo (apenas o tempo entre o clean e o build do JAR).

if [ -z "$1" ]; then
    echo "Erro: é necessário fornecer o caminho do arquivo JAR como parâmetro."
    echo "Exemplo de uso: $0 /caminho/do/arquivo.jar"
    exit 1
fi

jar="$1"
servidor_pid=""

inicia_servidor() {
    java -jar "$jar" &
    servidor_pid=$! # $! contém o PID do último processo em background
}

shutdown_suave_do_servidor() {
    if [ -n "$servidor_pid" ]; then
        echo "Enviando sinal SIGUSR1 para o servidor no PID: $servidor_pid"
        kill -SIGUSR1 "$servidor_pid"
    fi
}

servidor_em_execucao() {
    if ps -p "$servidor_pid" >/dev/null; then
        return 0
    else
        return 1
    fi
}

# Se o launcher for finalizado, finaliza o servidor também
trap 'shutdown_suave_do_servidor; exit 0' SIGTERM

###### O script efetivamente começa aqui ######

# Verifica se o arquivo JAR existe

while true; do
    inicia_servidor
    inotifywait -e delete_self "$jar"
    shutdown_suave_do_servidor
    while [ ! -e "$jar" ]; do
        sleep 1
    done
    sleep 2 # Para ter certeza que o jar foi completamente salvo
done
