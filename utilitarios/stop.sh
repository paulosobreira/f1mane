#!/bin/bash
PID=$(fuser 8080/tcp 2>/dev/null)
if [ -z "$PID" ]; then
  echo "Nada rodando na porta 8080."
else
  kill -9 $PID
  echo "Aplicação encerrada (PID $PID)."
fi
