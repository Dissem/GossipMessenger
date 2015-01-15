#!/bin/sh
echo "Bitte stellen Sie sicher, dass die Umgebungsvariable MPJ_HOME gesetzt"
echo "und mpjrun.sh direkt aus der Konsole ausführbar ist."
echo "Andernfalls müssen Sie eventuell dieses Skript anpassen."
echo
./gradlew build && mpjrun.sh -np 10 -cp libs/mpj.jar:build/libs/Gossip-1.0.jar ch.dissem.mpj.gossip.messageboard.Starter