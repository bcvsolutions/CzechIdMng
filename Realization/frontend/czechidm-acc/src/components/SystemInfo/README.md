# SystemInfo Component

Information about target system. If system doesn't exist, then short system's identifier will be shown.

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
<Advanced.SystemInfo entity={{ name: 'system' }}/>
```

or

```html
<Advanced.SystemInfo entityIdentifier="system" />
```
