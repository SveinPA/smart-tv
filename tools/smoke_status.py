
# Simple smoke test for the TV server.

# Run as terminal command: python tools/smoke_status.py localhost 1238
# (or just "python tools/smoke_status.py" to use defaults)
import socket, sys

host = sys.argv[1] if len(sys.argv) > 1 else "localhost"
port = int(sys.argv[2]) if len(sys.argv) > 2 else 1238

with socket.create_connection((host, port), timeout=3) as s:
    s.sendall(b"STATUS\r\n")
    data = s.recv(1024).decode("utf-8", "replace").strip()  # strip CRLF
    print("> STATUS")
    print("<", data)
    if data not in ("OK OFF", "OK ON"):
        raise SystemExit("Unexpected reply: " + data)
print("Smoke OK.")
