# IdentitiesInfo component

Show multiple identities (candidates) in one cell. Component use standard IdentityInfo component inner.

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| identities | Array of UUIDs or whole identities. | Array of UUIDs or whole identities. |  |
| isUsedIdentifier | bool  |  If true, then given identities are only identifiers, otherwise is given whole object. | true |
| maxEntry | int  |  Number of candidates who will be showing before will be using '...'  |  |
| showOnlyUsername | bool | If true, then show only username instead niceLabel | true |


## Usage

```html
<IdentitiesInfo identities={entity.candicateUsers} maxEntry={2} />
```
