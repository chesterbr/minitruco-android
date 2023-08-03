#!/bin/bash

# Inicia o servidor do miniTruco e o reinicia quando o JAR é modificado.
#
# Esse reinício é feito com um "soft shutdown" do servidor (enviando um SIGUSR1
# para que ele libere a porta) e subindo uma nova instância imediatamente*; dessa
# forma os jogadores podem finalizar partidas em andamento, mas novas conexões
# vão para o servidor novo.
#
# * na real uns 2-3 segundos pra garantir que o .jar novo está finalizado

if [ -z "$1" ]; then
    echo "Erro: é necessário fornecer o caminho do arquivo JAR como parâmetro."
    echo "Exemplo de uso: $0 /caminho/do/arquivo.jar"
    exit 1
fi

jar="$1"
servidor_pid=""

inicia_servidor() {
    java --enable-preview -jar "$jar" &
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

aguarda_mudanca_no_jar() {
    (inotifywait -e close_write "$jar") & # em background para não bloquer o SIGTERM
    inotify_pid=$!
    wait $inotify_pid
}

aguarda_o_novo_jar_estar_pronto() {
    while [ ! -e "$jar" ]; do
        sleep 1
    done
    sleep 2 # Para ter certeza que o jar foi completamente salvo
}

# Se o launcher for finalizado, finaliza o servidor também
trap 'shutdown_suave_do_servidor; exit 0' SIGTERM

###### O script efetivamente começa aqui ######

while true; do
    inicia_servidor
    aguarda_mudanca_no_jar
    shutdown_suave_do_servidor
    aguarda_o_novo_jar_estar_pronto
done
