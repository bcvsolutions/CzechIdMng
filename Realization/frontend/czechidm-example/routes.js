module.exports = {
  module: 'example',
  childRoutes: [
    {
      path: '/example/content',
      component: require('./src/content/ExampleContent')
    },
    {
      path: '/example/components',
      component: require('./src/content/ExampleComponents')
    },
    {
      path: '/example/products',
      component: require('./src/content/example-product/ExampleProducts'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['EXAMPLEPRODUCT_READ'] } ]
    },
    {
      path: 'example/product/:entityId/',
      component: require('./src/content/example-product/ExampleProductRoute'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['EXAMPLEPRODUCT_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/example-product/ExampleProductContent'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['EXAMPLEPRODUCT_READ'] } ]
        }
      ]
    },
    {
      path: 'example/product/:entityId/new',
      component: require('./src/content/example-product/ExampleProductContent'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['EXAMPLEPRODUCT_CREATE'] } ]
    },
    {
      path: 'example/form/identity-projection/:entityId',
      component: require('./src/content/identity/projection/ExampleIdentityProjection'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ' ] } ]
    },
    {
      path: 'example/form/combined-identity-projection/:entityId',
      component: require('./src/content/identity/projection/ExampleCombinedIdentityProjection'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ' ] } ]
    }
  ]
};
