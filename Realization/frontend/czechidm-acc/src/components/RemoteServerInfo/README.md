# RemoteServerInfo Component

Information about remote connector server. If server doesn't exist, then short identifier will be shown.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| entity | object  |  Selected entity - externally loaded.  Has higher priority, when is given, then loading is not needed. |  |
| entityIdentifier | string  |  Selected entity's identifier - entity will be loaded automatically.  |  |
| face | oneOf(['link'])  |  Decorator: <ul><li>TODO:  `text`: entity's nice label only</li><li>`link`: entity's nice label with link to detail</li><li>TODO: `full`: full info card</li></ul>  |  full |
| showLink | bool | Shows link to entity's detail | true |


## Usage

```html
<Advanced.RemoteServerInfo entity={{ host: 'server', port: 99 }}/>
```

or

```html
<Advanced.RemoteServerInfo entityIdentifier="uuid" />
```
