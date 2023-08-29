# Cisco ASA-5-111010 event translation to OCSF base event

To parse and translate the example event, use the following command:

```bash
bin/ocsf-cli -p cisco:syslog -R examples -r asa/111010 examples/cisco/asa/111010/raw.event
```

You can choose to run the parse and translate tasks independently:

## Step 1: Parse the raw event.

The parser converts the event to JSON format.

```bash
bin/ocsf-cli -p cisco:syslog examples/cisco/asa/111010/raw.event
```

## Step 2: Translate the parsed event

Assuming the parsed event is saved as `examples/cisco/asa/111010/parsed.json`.

```bash
bin/ocsf-cli -R examples -r asa/111010 examples/cisco/asa/111010/parsed.json
```
