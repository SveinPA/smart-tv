
# Simple smoke test for the TV server.

# Run as terminal command: python tools/smoke_status.py localhost 1238
# (or just "python tools/smoke_status.py" to use defaults)
import socket, sys

host = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
port = int(sys.argv[2]) if len(sys.argv) > 2 else 1238

with socket.create_connection((host, port), timeout=3) as s:
    def send(line: str) -> str:
        s.sendall((line + "\r\n").encode("utf-8"))
        return s.recv(1024).decode("utf-8", "replace").strip()

    # STATUS
    print("> STATUS"); print("<", send("STATUS"))

    # ON + STATUS
    print("> ON"); print("<", send("ON"))
    print("> STATUS"); print("<", send("STATUS"))

    # CHANNELS + GET
    print("> CHANNELS"); print("<", send("CHANNELS"))
    print("> GET"); print("<", send("GET"))

    # SET 5 + GET
    print("> SET 5"); print("<", send("SET 5"))
    print("> GET"); print("<", send("GET"))
    
    # UP + DOWN
    print("> UP");   print("<", send("UP"))
    print("> DOWN"); print("<", send("DOWN"))

print("Smoke OK.")

