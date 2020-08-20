# ScriptArea component

Component for editing code (script). Used mainly for editing Groovy and Javascript. Extended from AbstractFormComponent.

## Parameters
All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| helpBlock  | string   | Long description show under this component|  |
| mode  | string   | Type of script. Supported are only 'javascript', 'groovy', 'json' formats  | 'groovy' |
| height  | string   | Height of editor  | '10em' |
| showMaximalizationBtn  | boolean   | Show button for edit script in modal window  | true |
| completers  | array   | Array of custom suggestions for this editor |  |


## Usage

```html
<Basic.ScriptArea
  ref="transformToResourceScript"
  mode="groovy"
  helpBlock={this.i18n('acc:entity.SchemaAttributeHandling.transformToResourceScript.help')}
  label={this.i18n('acc:entity.SchemaAttributeHandling.transformToResourceScript.label')}/>
```

## Completer structure
```html
  {
    name: 'context.getIdentityRole()', // This value is show in the whispering dialog.
    returnType: 'List<IdmIdentityRoleDto>',
    description: 'Get all assigned identity roles.'
  }
```
