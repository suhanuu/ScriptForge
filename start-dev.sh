#!/bin/bash
echo "Starting ScriptForge development..."
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:5173"
echo ""

# Start backend
cd backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

# Start frontend
cd frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "Backend PID: $BACKEND_PID"
echo "Frontend PID: $FRONTEND_PID"
echo "Press Ctrl+C to stop both"

trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT TERM
wait
