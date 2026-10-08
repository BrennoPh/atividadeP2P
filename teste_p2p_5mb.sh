#!/bin/bash
NUM_CLIENTES=10
ARQUIVO_TORRENT="arquivo_5mb.torrent"

echo "Iniciando $NUM_CLIENTES clientes P2P (BitTorrent)..."
rm -f tempos_p2p.txt

for i in $(seq 1 $NUM_CLIENTES); do
    mkdir -p cliente_$i
    (
        inicio=$(date +%s%3N)
        # --seed-time=0 faz o cliente sair da rede assim que chegar a 100%
        aria2c --listen-port=$((6000+i)) --dht-listen-port=$((6000+i)) \
               --bt-enable-lpd=true --seed-time=0 -d ./cliente_$i \
               --console-log-level=error -q $ARQUIVO_TORRENT
        
        fim=$(date +%s%3N)
        tempo=$((fim - inicio))
        echo $tempo >> tempos_p2p.txt
        echo "Cliente $i concluiu em $tempo ms"
    ) &
done

wait

echo -e "\n=== RESULTADOS P2P ($NUM_CLIENTES Clientes) ==="
awk '{
    if(min==""){min=max=$1}
    if($1>max){max=$1}
    if($1<min){min=$1}
    soma+=$1
    count+=1
} END {
    print "Tempo Mínimo: " min " ms"
    printf "Tempo Médio:  %.2f ms\n", soma/count
    print "Tempo Máximo: " max " ms"
}' tempos_p2p.txt
echo "========================================="

# Limpeza: descarta os ficheiros baixados para poupar disco
rm -rf cliente_*
