const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-cvmakers-357bfbbf-1caa-475a-931a-ae6d024b80a2";

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      databaseId: DATABASE_ID,
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read administrators or audit logs", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("administrators").get());
  await assertFails(unauthedDb.collection("audit_logs").get());
});

test("Initial Super Admin (yahy38989@gmail.com) can bootstrap administrator profile and read admin collections", async () => {
  const superAdminDb = testEnv.authenticatedContext("admin_uid_1", {
    email: "yahy38989@gmail.com"
  }).firestore();

  // Create admin profile
  await assertSucceeds(superAdminDb.collection("administrators").doc("admin_uid_1").set({
    id: "admin_uid_1",
    email: "yahy38989@gmail.com",
    displayName: "Super Admin",
    role: "super_admin",
    isActive: true,
    createdAt: new Date()
  }));

  // Read administrators
  await assertSucceeds(superAdminDb.collection("administrators").get());
});

test("Normal user cannot modify other users or approve templates", async () => {
  const normalUserDb = testEnv.authenticatedContext("user_abc", {
    email: "user@example.com"
  }).firestore();

  // Cannot modify other user
  await assertFails(normalUserDb.collection("users").doc("user_xyz").set({
    id: "user_xyz",
    status: "suspended"
  }));

  // Cannot update template status
  await assertFails(normalUserDb.collection("templates").doc("tmpl_1").update({
    status: "approved"
  }));
});
