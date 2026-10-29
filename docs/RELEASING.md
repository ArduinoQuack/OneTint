# Making a release

1. Update the manifest version and release notes.
2. Run the tests and signed build described in [BUILDING.md](BUILDING.md).
3. Check Apply, Restore, the previews and app-icon recoloring on the phone.
4. Tag the commit and create a GitHub release. Attach the signed APK and its SHA-256 checksum.

Keep untested builds marked as pre-releases. Never upload the signing key, signing backup, local app data or Samsung APKs.

Updates must use the existing signing key. Its certificate SHA-256 is:

```text
9a45c844f96fb13b81598ba461ac84b2fb16dad09b36874328ad0acc86ae56a3
```
