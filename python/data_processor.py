import time
import requests
import asyncio


class DataProcessor:
    def __init__(self):
        self.cache = {}
        self.results = []

    # PERF ISSUE: Nested loop with list lookup - O(n^2)
    def find_matching_records(self, list_a, list_b):
        matches = []
        for item_a in list_a:
            for item_b in list_b:
                if item_a["id"] == item_b["id"]:
                    matches.append({**item_a, **item_b})
        return matches

    # PERF ISSUE: String concatenation in loop
    def build_csv(self, records):
        csv_output = ""
        for record in records:
            line = ""
            for key, value in record.items():
                line += str(value) + ","
            csv_output += line[:-1] + "\n"
        return csv_output

    # PERF ISSUE: Loading entire file into memory
    def process_large_file(self, filepath):
        with open(filepath, "r") as f:
            content = f.read()  # Loads entire file into memory

        lines = content.split("\n")
        results = []
        for line in lines:
            if "ERROR" in line:
                results.append(line)
        return results

    # PERF ISSUE: Synchronous I/O in async function
    async def fetch_user_data(self, user_ids):
        results = []
        for uid in user_ids:
            # Using synchronous requests in async context
            response = requests.get(f"https://api.example.com/users/{uid}")
            results.append(response.json())
            time.sleep(0.1)  # Blocking sleep in async
        return results

    # PERF ISSUE: Global mutable state
    global_registry = {}

    def register_handler(self, name, handler):
        DataProcessor.global_registry[name] = handler

    # PERF ISSUE: Repeated expensive computation without caching
    def get_user_permissions(self, user_id, resource_id):
        # This hits the DB every single time, no caching
        permissions = self._fetch_from_db(user_id)
        roles = self._fetch_roles_from_db(user_id)
        resource_acl = self._fetch_acl_from_db(resource_id)

        # Complex computation repeated every call
        effective = []
        for perm in permissions:
            for role in roles:
                for acl in resource_acl:
                    if perm["level"] >= acl["required_level"]:
                        effective.append(perm)

        return effective

    # PERF ISSUE: Creating connection per request
    def save_records(self, records):
        import sqlite3
        for record in records:
            conn = sqlite3.connect("mydb.sqlite")
            cursor = conn.cursor()
            cursor.execute(
                "INSERT INTO records VALUES (?, ?, ?)",
                (record["id"], record["name"], record["value"])
            )
            conn.commit()
            conn.close()

    # PERF ISSUE: List comprehension creating unnecessary intermediate lists
    def transform_data(self, raw_data):
        step1 = [item for item in raw_data if item["active"]]
        step2 = [item["value"] for item in step1]
        step3 = [v * 2 for v in step2]
        step4 = [v for v in step3 if v > 100]
        return step4

    def _fetch_from_db(self, user_id):
        return [{"level": 1}, {"level": 2}]

    def _fetch_roles_from_db(self, user_id):
        return [{"name": "admin"}, {"name": "user"}]

    def _fetch_acl_from_db(self, resource_id):
        return [{"required_level": 1}]
