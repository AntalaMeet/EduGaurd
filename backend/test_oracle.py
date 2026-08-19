import os
from dotenv import load_dotenv
import oracledb

load_dotenv()
oracledb.init_oracle_client(
    lib_dir=r"C:\oracle\instantclient_23_26"
)

print("Oracle Client loaded")
print("Thin mode:", oracledb.is_thin_mode())

connection = oracledb.connect(
    user=os.getenv("ORACLE_USER"),
    password=os.getenv("ORACLE_PASSWORD"),
    dsn="localhost:1521/XE"
)

cursor = connection.cursor()

cursor.execute("SELECT COUNT(*) FROM students")

result = cursor.fetchone()

print("Students:", result[0])

cursor.close()
connection.close()

print("Oracle connection successful!")