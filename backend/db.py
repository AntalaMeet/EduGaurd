import os
from dotenv import load_dotenv
import oracledb

load_dotenv()

oracledb.init_oracle_client(
    lib_dir=r"C:\oracle\instantclient_23_26"
)

ORACLE_USER = os.getenv("ORACLE_USER")
ORACLE_PASSWORD = os.getenv("ORACLE_PASSWORD")
ORACLE_HOST = os.getenv("ORACLE_HOST", "localhost")
ORACLE_PORT = os.getenv("ORACLE_PORT", "1521")
ORACLE_SERVICE = os.getenv("ORACLE_SERVICE", "XE")


def get_connection():

    dsn = oracledb.makedsn(
        ORACLE_HOST,
        ORACLE_PORT,
        service_name=ORACLE_SERVICE
    )

    return oracledb.connect(
        user=ORACLE_USER,
        password=ORACLE_PASSWORD,
        dsn=dsn
    )