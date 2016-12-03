
require('reflect-metadata');

var ss = require('../../../target/js/stack.service');

describe('Stack service', function() {
    it('can be created', function() {
        var stack = new ss.StackService();
        expect(stack).toBeDefined();
    });

    it('values can be pushed', function() {
        var stack = new ss.StackService();
        stack.push('value');
    });

    it('popping when empty does nothing', function() {
        var stack = new ss.StackService();
        expect(stack.pop()).toBeUndefined();
    });

    it('a value can be pushed and then popped', function() {
        var stack = new ss.StackService();
        stack.push('value');
        expect(stack.pop()).toBe('value');
        expect(stack.pop()).toBeUndefined();
    });

    it('multiple values can be pushed and then popped', function() {
        var stack = new ss.StackService();
        stack.push('value0');
        stack.push('value1');
        expect(stack.pop()).toBe('value1');
        expect(stack.pop()).toBe('value0');
        expect(stack.pop()).toBeUndefined();
    });
});
