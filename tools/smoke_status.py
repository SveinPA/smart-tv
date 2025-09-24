
# Simple smoke test for the TV server.

# Run as terminal command: python tools/smoke_status.py localhost 1238
# (or just "python tools/smoke_status.py" to use defaults)
import socket, sys

host = sys.argv[1] if len(sys.argv) > 1 else "localhost"
port = int(sys.argv[2]) if len(sys.argv) > 2 else 1238

with socket.create_connection((host, port), timeout=3) as s:
    def send(line: str) -> str:
        s.sendall((line + "\r\n").encode())
        return s.recv(1024).decode("utf-8", "replace").strip()

    print("> STATUS"); print("<", send("STATUS"))
    print("> ON");     print("<", send("ON"))
    print("> STATUS"); print("<", send("STATUS"))
    print("> OFF");    print("<", send("OFF"))
    print("> STATUS"); print("<", send("STATUS"))

print("Smoke OK.")
