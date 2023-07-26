# Translating Windows Multiline Event 4103 (Powershell Module Logging)

To parse and translate the example event, use the following command:
```bash
bin/ocsf-cli -p microsoft:windows:multiline -R examples -r multiline/4103 examples/microsoft/windows/multiline/4103/raw.event
```

You can choose to run the parse and translate tasks independently:

## Step 1: Parse the raw event.
The parser converts the event to JSON format.
```bash
bin/ocsf-cli -p microsoft:windows:multiline examples/microsoft/windows/multiline/4103/raw.event
```

## Step 2: Translate the parsed event
Assuming the parsed event is saved as `examples/microsoft/windows/multiline/4103/parsed.json`.

```bash
bin/ocsf-cli -R examples -r multiline/4103 examples/microsoft/windows/multiline/4103/parsed.json
```
